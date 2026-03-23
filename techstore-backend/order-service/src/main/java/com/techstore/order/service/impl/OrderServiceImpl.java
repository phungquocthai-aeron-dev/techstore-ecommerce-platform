package com.techstore.order.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.techstore.event.dto.PostEvent;
import com.techstore.order.client.ProductServiceClient;
import com.techstore.order.client.UserServiceClient;
import com.techstore.order.client.WarehouseServiceClient;
import com.techstore.order.dto.request.InventoryExportRequest;
import com.techstore.order.dto.request.OrderCreateRequest;
import com.techstore.order.dto.request.OrderItemRequest;
import com.techstore.order.dto.response.AdminOrderResponse;
import com.techstore.order.dto.response.ApiResponse;
import com.techstore.order.dto.response.CustomerOrderItemResponse;
import com.techstore.order.dto.response.CustomerOrderResponse;
import com.techstore.order.dto.response.CustomerResponse;
import com.techstore.order.dto.response.OrderDetailResponse;
import com.techstore.order.dto.response.OrderResponse;
import com.techstore.order.dto.response.OrderSummaryResponse;
import com.techstore.order.dto.response.ProductSalesResponse;
import com.techstore.order.dto.response.RevenueStatsResponse;
import com.techstore.order.dto.response.ShippingInfo;
import com.techstore.order.dto.response.TopLoyalCustomerResponse;
import com.techstore.order.dto.response.TopVariantResponse;
import com.techstore.order.dto.response.VariantInfo;
import com.techstore.order.entity.Address;
import com.techstore.order.entity.Coupon;
import com.techstore.order.entity.Order;
import com.techstore.order.entity.OrderDetail;
import com.techstore.order.entity.PaymentMethod;
import com.techstore.order.entity.Refund;
import com.techstore.order.entity.ShippingProvider;
import com.techstore.order.exception.AppException;
import com.techstore.order.exception.ErrorCode;
import com.techstore.order.mapper.OrderMapper;
import com.techstore.order.repository.AddressRepository;
import com.techstore.order.repository.CouponRepository;
import com.techstore.order.repository.OrderDetailRepository;
import com.techstore.order.repository.OrderRepository;
import com.techstore.order.repository.PaymentMethodRepository;
import com.techstore.order.repository.PaymentRepository;
import com.techstore.order.repository.ShippingProviderRepository;
import com.techstore.order.service.OrderService;
import com.techstore.order.service.payment.PaymentStrategy;
import com.techstore.order.service.payment.PaymentStrategyFactory;
import com.techstore.order.service.shipping.ShippingFactory;
import com.techstore.order.service.shipping.ShippingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final WarehouseServiceClient warehouseClient;
    private final ProductServiceClient productClient;
    private final UserServiceClient userClient;
    private final OrderMapper mapper;
    private final PaymentStrategyFactory paymentFactory;
    private final ShippingFactory shippingFactory;

    private final ShippingProviderRepository shippingProviderRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CouponRepository couponRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private enum Period {
        TODAY,
        MONTH,
        QUARTER,
        YEAR,
        CUSTOM
    }

    private static final Set<String> REVENUE_STATUSES = Set.of("PROCESSING", "READY_TO_SHIP", "SHIPPING", "DELIVERED");

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER', 'SALES_STAFF')")
    public List<CustomerOrderResponse> getOrdersByCustomer(Long customerId, String status) {

        List<Order> orders;

        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            orders = orderRepository.findByCustomerId(customerId);
        } else {
            orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        }

        return orders.stream()
                .map(order -> {
                    List<Long> variantIds = order.getOrderDetails().stream()
                            .map(OrderDetail::getVariantId)
                            .toList();

                    List<VariantInfo> variants =
                            productClient.getVariantsByIds(variantIds).getResult();

                    Map<Long, VariantInfo> variantMap =
                            variants.stream().collect(Collectors.toMap(VariantInfo::getId, v -> v));

                    List<CustomerOrderItemResponse> items = order.getOrderDetails().stream()
                            .map(detail -> {
                                VariantInfo variant = variantMap.get(detail.getVariantId());

                                return CustomerOrderItemResponse.builder()
                                        .orderDetailId(detail.getId())
                                        .variantId(detail.getVariantId())
                                        .name(detail.getName())
                                        .image(variant != null ? variant.getImageUrl() : null)
                                        .quantity(detail.getQuantity())
                                        .price(detail.getPrice())
                                        .reviewed(detail.getReviewed())
                                        .build();
                            })
                            .toList();

                    return CustomerOrderResponse.builder()
                            .orderId(order.getId())
                            .totalPrice(order.getTotalPrice())
                            .shippingFee(order.getShippingFee())
                            .vat(order.getVat())
                            .status(order.getStatus())
                            .shippingCode(order.getShippingCode())
                            .createdAt(order.getCreatedAt())
                            .expectedDeliveryTime(order.getExpectedDeliveryTime())
                            .shippingProviderName(
                                    order.getShippingProvider() != null
                                            ? order.getShippingProvider().getName()
                                            : null)
                            .couponName(
                                    order.getCoupon() != null
                                            ? order.getCoupon().getName()
                                            : null)
                            .address(
                                    order.getAddress() != null
                                            ? order.getAddress().getAddress()
                                                    + ", "
                                                    + order.getAddress().getWardName()
                                                    + ", "
                                                    + order.getAddress().getDistrictName()
                                                    + ", "
                                                    + order.getAddress().getProvinceName()
                                            : null)
                            .items(items)
                            .build();
                })
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public List<AdminOrderResponse> getAllOrders(String status) {

        List<Order> orders;

        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findByStatus(status);
        }

        return orders.stream()
                .map(order -> {

                    // ===== Lấy customer info =====
                    CustomerResponse customer =
                            userClient.getCustomerById(order.getCustomerId()).getResult();

                    // ===== Lấy variant info =====
                    List<Long> variantIds = order.getOrderDetails().stream()
                            .map(OrderDetail::getVariantId)
                            .toList();

                    List<VariantInfo> variants =
                            productClient.getVariantsByIds(variantIds).getResult();

                    Map<Long, VariantInfo> variantMap =
                            variants.stream().collect(Collectors.toMap(VariantInfo::getId, v -> v));

                    List<CustomerOrderItemResponse> items = order.getOrderDetails().stream()
                            .map(detail -> {
                                VariantInfo variant = variantMap.get(detail.getVariantId());

                                return CustomerOrderItemResponse.builder()
                                        .orderDetailId(detail.getId())
                                        .variantId(detail.getVariantId())
                                        .name(detail.getName())
                                        .image(variant != null ? variant.getImageUrl() : null)
                                        .quantity(detail.getQuantity())
                                        .reviewed(detail.getReviewed())
                                        .price(detail.getPrice())
                                        .build();
                            })
                            .toList();

                    return AdminOrderResponse.builder()
                            .orderId(order.getId())
                            .customerId(order.getCustomerId())
                            .customerName(customer.getFullName())
                            .customerEmail(customer.getEmail())
                            .customerPhone(customer.getPhone())
                            .totalPrice(order.getTotalPrice())
                            .shippingFee(order.getShippingFee())
                            .vat(order.getVat())
                            .status(order.getStatus())
                            .shippingCode(order.getShippingCode())
                            .createdAt(order.getCreatedAt())
                            .expectedDeliveryTime(order.getExpectedDeliveryTime())
                            .shippingProviderName(
                                    order.getShippingProvider() != null
                                            ? order.getShippingProvider().getName()
                                            : null)
                            .couponName(
                                    order.getCoupon() != null
                                            ? order.getCoupon().getName()
                                            : null)
                            .address(
                                    order.getAddress() != null
                                            ? order.getAddress().getAddress()
                                                    + ", "
                                                    + order.getAddress().getWardName()
                                                    + ", "
                                                    + order.getAddress().getDistrictName()
                                                    + ", "
                                                    + order.getAddress().getProvinceName()
                                            : null)
                            .items(items)
                            .build();
                })
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public OrderResponse createOrder(OrderCreateRequest request, String ipAddress) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus("CREATED");

        List<Long> variantIds =
                request.getItems().stream().map(OrderItemRequest::getVariantId).toList();

        ApiResponse<List<VariantInfo>> response = productClient.getVariantsByIds(variantIds);
        List<VariantInfo> variants = response.getResult();

        Map<Long, VariantInfo> variantMap = variants.stream().collect(Collectors.toMap(VariantInfo::getId, v -> v));

        List<OrderDetail> details = new ArrayList<>();
        double total = 0;

        for (OrderItemRequest item : request.getItems()) {

            VariantInfo variant = variantMap.get(item.getVariantId());

            if (variant == null) {
                throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            }

            OrderDetail detail = OrderDetail.builder()
                    .variantId(item.getVariantId())
                    .name(variant.getName())
                    .quantity(item.getQuantity().intValue())
                    .price(variant.getPrice())
                    .totalWeight(variant.getWeight() * item.getQuantity().intValue())
                    .reviewed(false)
                    .status("ACTIVE")
                    .order(order)
                    .build();

            total += detail.getPrice() * detail.getQuantity();
            details.add(detail);
        }

        double discount = 0;

        if (request.getCouponId() != null) {

            Coupon coupon = couponRepository
                    .findById(request.getCouponId())
                    .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

            LocalDateTime now = LocalDateTime.now();

            if (!"ACTIVE".equalsIgnoreCase(coupon.getStatus())) {
                throw new AppException(ErrorCode.COUPON_INVALID);
            }

            if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
                throw new AppException(ErrorCode.COUPON_EXPIRED);
            }

            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                throw new AppException(ErrorCode.COUPON_LIMIT_REACHED);
            }

            if (coupon.getMinOrderValue() != null && total < coupon.getMinOrderValue()) {
                throw new AppException(ErrorCode.ORDER_NOT_ELIGIBLE_FOR_COUPON);
            }

            if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {

                discount = total * coupon.getDiscountValue() / 100;

                if (coupon.getMaxDiscount() != null) {
                    discount = Math.min(discount, coupon.getMaxDiscount());
                }

            } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {

                discount = coupon.getDiscountValue();
            }

            discount = Math.min(discount, total);

            total = total - discount;

            order.setCoupon(coupon);

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        }

        order.setOrderDetails(details);

        ShippingProvider shippingProvider = shippingProviderRepository
                .findById(request.getShippingProviderId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_PROVIDER_NOT_FOUND));

        order.setShippingProvider(shippingProvider);

        Address address = addressRepository
                .findById(request.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        order.setAddress(address);

        if (request.getPaymentMethodId() != null) {

            PaymentMethod paymentMethod = paymentMethodRepository
                    .findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

            order.setPaymentMethod(paymentMethod);
        }

        double totalWeight =
                details.stream().mapToDouble(OrderDetail::getTotalWeight).sum();

        ShippingService shippingService = shippingFactory.getService(shippingProvider.getCode());

        double shippingFee = 0;

        try {
            shippingFee = shippingService.calculateFee(request.getAddressId(), totalWeight);
        } catch (Exception e) {
            throw new AppException(ErrorCode.SHIPPING_FEE_CALCULATION_FAILED);
        }

        order.setShippingFee(shippingFee);

        order.setVat(total * 0.1);

        order.setTotalPrice(total + order.getVat() + shippingFee);

        orderRepository.save(order);

        String paymentUrl = null;

        if (request.getPaymentMethod() != null) {

            PaymentStrategy strategy = paymentFactory.getStrategy(request.getPaymentMethod());

            paymentUrl = strategy.createPaymentUrl(order, ipAddress);
        }

        List<Long> transactionIds = warehouseClient
                .exportInventory(InventoryExportRequest.builder()
                        .orderId(order.getId())
                        .items(request.getItems())
                        .build())
                .getResult();

        if (transactionIds != null && !transactionIds.isEmpty()) {
            String txString = transactionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            order.setWarehouseTransactionId(txString);
        }
        orderRepository.save(order);

        OrderResponse result = mapper.toDto(order);
        result.setPaymentUrl(paymentUrl);

        PostEvent event = PostEvent.builder()
                .title("Cập nhật trạng thái đơn hàng")
                .content(buildContent(order.getId(), order.getStatus()))
                .userId(order.getCustomerId().toString())
                .build();

        kafkaTemplate.send("post-delivery", event);

        return result;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public void confirmOrder(Long orderId, Long staffId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getShippingProvider() == null) {
            throw new AppException(ErrorCode.INVALID_SHIPPING_TYPE);
        }

        String shippingType = order.getShippingProvider().getCode();

        ShippingService shippingService = shippingFactory.getService(shippingType);

        Address address = order.getAddress();

        ShippingInfo shippingInfo = shippingService.createShippingOrder(order, address);

        order.setShippingCode(shippingInfo.getOrderCode());
        order.setShippingFee(shippingInfo.getShippingFee());
        order.setExpectedDeliveryTime(shippingInfo
                .getExpectedDeliveryTime()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .toLocalDateTime());
        order.setStaffId(staffId);

        order.setStatus("READY_TO_SHIP");

        PostEvent event = PostEvent.builder()
                .title("Cập nhật trạng thái đơn hàng")
                .content(buildContent(order.getId(), order.getStatus()))
                .userId(order.getCustomerId().toString())
                .build();

        kafkaTemplate.send("post-delivery", event);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public void cancelOrder(Long orderId, Long staffId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        order.setStaffId(staffId);
        order.setStatus("CANCELLED");

        // ===== Cancel nhiều warehouse transaction =====
        if (order.getWarehouseTransactionId() != null
                && !order.getWarehouseTransactionId().isBlank()) {

            List<Long> transactionIds = Arrays.stream(
                            order.getWarehouseTransactionId().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .toList();

            for (Long txId : transactionIds) {
                warehouseClient.cancelTransaction(txId);
            }
        }

        // ===== Refund nếu có payment =====
        paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);
        });

        PostEvent event = PostEvent.builder()
                .title("Cập nhật trạng thái đơn hàng")
                .content(buildContent(order.getId(), order.getStatus()))
                .userId(order.getCustomerId().toString())
                .build();

        kafkaTemplate.send("post-delivery", event);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public void updateStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(status);

        PostEvent event = PostEvent.builder()
                .title("Cập nhật trạng thái đơn hàng")
                .content(buildContent(order.getId(), status))
                .userId(order.getCustomerId().toString())
                .build();

        kafkaTemplate.send("post-delivery", event);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public void createRefund(Long detailId, String reason, Long staffId) {

        OrderDetail detail = orderDetailRepository
                .findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        Refund.builder()
                .reason(reason)
                .refundAmount(detail.getPrice() * detail.getQuantity())
                .status("CREATED")
                .staffId(staffId)
                .orderDetail(detail)
                .build();

        detail.setStatus("RETURNED");

        if (detail.getOrder().getOrderDetails().stream()
                .allMatch(d -> d.getStatus().equals("RETURNED"))) {

            detail.getOrder().setStatus("RETURNED");
        } else {
            detail.getOrder().setStatus("PARTIALLY_RETURNED");
        }

        PostEvent event = PostEvent.builder()
                .title("Cập nhật trạng thái đơn hàng")
                .content(buildContent(
                        detail.getOrder().getId(), detail.getOrder().getStatus()))
                .userId(detail.getOrder().getCustomerId().toString())
                .build();

        kafkaTemplate.send("post-delivery", event);
    }

    @Override
    public String printLabel(Long orderId) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        ShippingService shippingService =
                shippingFactory.getService(order.getShippingProvider().getCode());

        return shippingService.generatePrintUrl(order);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long detailId) {
        OrderDetail orderDetail = orderDetailRepository
                .findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));
        return OrderDetailResponse.builder()
                .id(orderDetail.getId())
                .quantity(orderDetail.getQuantity())
                .name(orderDetail.getName())
                .price(orderDetail.getPrice())
                .status(orderDetail.getStatus())
                .totalWeight(orderDetail.getTotalWeight())
                .variantId(orderDetail.getVariantId())
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public RevenueStatsResponse getRevenueStats(String period, LocalDate from, LocalDate to) {

        Period p = period == null ? Period.MONTH : Period.valueOf(period.toUpperCase());

        LocalDateTime dtFrom;
        LocalDateTime dtTo;

        switch (p) {
            case TODAY: {
                LocalDate today = LocalDate.now();
                dtFrom = today.atStartOfDay();
                dtTo = today.atTime(23, 59, 59);
                break;
            }
            case MONTH: {
                LocalDate start = (from != null) ? from : LocalDate.now().withDayOfMonth(1);
                LocalDate end = (to != null) ? to : LocalDate.now();
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            case QUARTER: {
                LocalDate now = LocalDate.now();
                int q = (now.getMonthValue() - 1) / 3;
                LocalDate start =
                        (from != null) ? from : now.withMonth(q * 3 + 1).withDayOfMonth(1);
                LocalDate end = (to != null) ? to : now;
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            case YEAR: {
                LocalDate start = (from != null) ? from : LocalDate.now().withDayOfYear(1);
                LocalDate end = (to != null) ? to : LocalDate.now();
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            default: {
                if (from == null || to == null) throw new AppException(ErrorCode.INVALID_DATE_RANGE);
                dtFrom = from.atStartOfDay();
                dtTo = to.atTime(23, 59, 59);
            }
        }

        List<Object[]> rows =
                switch (p) {
                    case TODAY -> orderRepository.revenueByDay(dtFrom, dtTo);
                    case MONTH -> orderRepository.revenueByMonth(dtFrom, dtTo);
                    case QUARTER -> orderRepository.revenueByQuarter(dtFrom, dtTo);
                    case YEAR -> orderRepository.revenueByYear(dtFrom, dtTo);
                    case CUSTOM -> {
                        long days = ChronoUnit.DAYS.between(from, to);
                        yield days <= 31
                                ? orderRepository.revenueByDay(dtFrom, dtTo)
                                : orderRepository.revenueByMonth(dtFrom, dtTo);
                    }
                };

        List<RevenueStatsResponse.RevenueDataPoint> dataPoints = rows.stream()
                .map(r -> RevenueStatsResponse.RevenueDataPoint.builder()
                        .label(String.valueOf(r[0]))
                        .revenue(((Number) r[1]).doubleValue())
                        .orderCount(((Number) r[2]).longValue())
                        .build())
                .toList();

        double totalRevenue = dataPoints.stream()
                .mapToDouble(RevenueStatsResponse.RevenueDataPoint::getRevenue)
                .sum();
        long totalOrders = dataPoints.stream()
                .mapToLong(RevenueStatsResponse.RevenueDataPoint::getOrderCount)
                .sum();

        return RevenueStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .dataPoints(dataPoints)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public List<TopVariantResponse> getTopVariants(int top, LocalDate from, LocalDate to) {

        LocalDateTime dtFrom = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime dtTo = (to != null) ? to.atTime(23, 59, 59) : null;

        int limit = (top <= 0) ? 10 : top;

        List<Object[]> rows = orderRepository.topVariants(dtFrom, dtTo, PageRequest.of(0, limit));

        List<Long> variantIds =
                rows.stream().map(r -> ((Number) r[0]).longValue()).toList();

        Map<Long, VariantInfo> variantMap = variantIds.isEmpty()
                ? Map.of()
                : productClient.getVariantsByIds(variantIds).getResult().stream()
                        .collect(Collectors.toMap(VariantInfo::getId, v -> v));

        return rows.stream()
                .map(r -> {
                    Long variantId = ((Number) r[0]).longValue();
                    VariantInfo info = variantMap.get(variantId);
                    return TopVariantResponse.builder()
                            .variantId(variantId)
                            .name(String.valueOf(r[1]))
                            .imageUrl(info != null ? info.getImageUrl() : null)
                            .totalQuantitySold(((Number) r[2]).longValue())
                            .totalRevenue(((Number) r[3]).doubleValue())
                            .build();
                })
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public OrderSummaryResponse getOrderSummary(String status, LocalDate from, LocalDate to) {

        LocalDateTime dtFrom =
                (from != null) ? from.atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime dtTo =
                (to != null) ? to.atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);

        String statusParam =
                (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) ? null : status.toUpperCase();

        List<Object[]> rows = orderRepository.orderSummaryByStatus(statusParam, dtFrom, dtTo);

        List<OrderSummaryResponse.StatusCount> breakdown = rows.stream()
                .map(r -> OrderSummaryResponse.StatusCount.builder()
                        .status(String.valueOf(r[0]))
                        .count(((Number) r[1]).longValue())
                        .revenue(((Number) r[2]).doubleValue())
                        .build())
                .toList();

        double totalRevenue = breakdown.stream()
                .filter(s -> REVENUE_STATUSES.contains(s.getStatus()))
                .mapToDouble(OrderSummaryResponse.StatusCount::getRevenue)
                .sum();

        long totalOrders = breakdown.stream()
                .mapToLong(OrderSummaryResponse.StatusCount::getCount)
                .sum();

        return OrderSummaryResponse.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .statusBreakdown(breakdown)
                .build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public List<TopLoyalCustomerResponse> getTopLoyalCustomers(int top, String period, LocalDate from, LocalDate to) {

        LocalDateTime dtFrom;
        LocalDateTime dtTo;

        Period p = period == null ? Period.MONTH : Period.valueOf(period.toUpperCase());

        switch (p) {
            case TODAY: {
                LocalDate today = LocalDate.now();
                dtFrom = today.atStartOfDay();
                dtTo = today.atTime(23, 59, 59);
                break;
            }
            case MONTH: {
                LocalDate start = (from != null) ? from : LocalDate.now().withDayOfMonth(1);
                LocalDate end = (to != null) ? to : LocalDate.now();
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            case QUARTER: {
                LocalDate now = LocalDate.now();
                int q = (now.getMonthValue() - 1) / 3;
                LocalDate start =
                        (from != null) ? from : now.withMonth(q * 3 + 1).withDayOfMonth(1);
                LocalDate end = (to != null) ? to : now;
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            case YEAR: {
                LocalDate start = (from != null) ? from : LocalDate.now().withDayOfYear(1);
                LocalDate end = (to != null) ? to : LocalDate.now();
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            default: {
                if (from == null || to == null) throw new AppException(ErrorCode.INVALID_DATE_RANGE);
                dtFrom = from.atStartOfDay();
                dtTo = to.atTime(23, 59, 59);
            }
        }

        int limit = (top <= 0) ? 10 : top;

        List<Object[]> rows = orderRepository.topLoyalCustomers(dtFrom, dtTo, PageRequest.of(0, limit));

        // Batch fetch customer info
        List<Long> customerIds =
                rows.stream().map(r -> ((Number) r[0]).longValue()).toList();

        Map<Long, CustomerResponse> customerMap = customerIds.stream().collect(Collectors.toMap(id -> id, id -> {
            try {
                return userClient.getCustomerById(id).getResult();
            } catch (Exception e) {
                log.warn("Không lấy được thông tin customer id={}", id);
                return null;
            }
        }));

        return rows.stream()
                .map(r -> {
                    Long customerId = ((Number) r[0]).longValue();
                    long orderCount = ((Number) r[1]).longValue();
                    double totalSpent = ((Number) r[2]).doubleValue();
                    double score = 0.4 * orderCount + 0.6 * totalSpent;

                    CustomerResponse customer = customerMap.get(customerId);

                    return TopLoyalCustomerResponse.builder()
                            .customerId(customerId)
                            .fullName(customer != null ? customer.getFullName() : null)
                            .email(customer != null ? customer.getEmail() : null)
                            .phone(customer != null ? customer.getPhone() : null)
                            .avatarUrl(customer != null ? customer.getAvatarUrl() : null)
                            .orderCount(orderCount)
                            .totalSpent(totalSpent)
                            .loyaltyScore(score)
                            .build();
                })
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES_STAFF')")
    public ProductSalesResponse getProductSales(Long productId, String period, LocalDate from, LocalDate to) {

        Period p = period == null ? Period.MONTH : Period.valueOf(period.toUpperCase());

        LocalDateTime dtFrom;
        LocalDateTime dtTo;

        switch (p) {
            case TODAY: {
                LocalDate today = LocalDate.now();
                dtFrom = today.atStartOfDay();
                dtTo = today.atTime(23, 59, 59);
                break;
            }
            case MONTH: {
                LocalDate start = (from != null) ? from : LocalDate.now().withDayOfMonth(1);
                LocalDate end = (to != null) ? to : LocalDate.now();
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            case QUARTER: {
                LocalDate now = LocalDate.now();
                int q = (now.getMonthValue() - 1) / 3;
                LocalDate start =
                        (from != null) ? from : now.withMonth(q * 3 + 1).withDayOfMonth(1);
                LocalDate end = (to != null) ? to : now;
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            case YEAR: {
                LocalDate start = (from != null) ? from : LocalDate.now().withDayOfYear(1);
                LocalDate end = (to != null) ? to : LocalDate.now();
                dtFrom = start.atStartOfDay();
                dtTo = end.atTime(23, 59, 59);
                break;
            }
            default: {
                if (from == null || to == null) throw new AppException(ErrorCode.INVALID_DATE_RANGE);
                dtFrom = from.atStartOfDay();
                dtTo = to.atTime(23, 59, 59);
            }
        }

        // Lấy danh sách variantId của product từ product-service
        List<Long> variantIds = productClient.getVariantsByProductId(productId).getResult().stream()
                .map(VariantInfo::getId)
                .toList();

        if (variantIds == null || variantIds.isEmpty()) {
            return ProductSalesResponse.builder()
                    .productId(productId)
                    .period(p.name())
                    .totalQuantitySold(0)
                    .variants(List.of())
                    .build();
        }

        // Chọn query phù hợp theo period
        List<Object[]> rows =
                switch (p) {
                    case TODAY -> orderRepository.productSalesByHour(variantIds, dtFrom, dtTo);
                    case MONTH -> orderRepository.productSalesByDay(variantIds, dtFrom, dtTo);
                    case QUARTER, YEAR -> orderRepository.productSalesByMonth(variantIds, dtFrom, dtTo);
                    case CUSTOM -> {
                        long days = ChronoUnit.DAYS.between(from, to);
                        yield days <= 31
                                ? orderRepository.productSalesByDay(variantIds, dtFrom, dtTo)
                                : orderRepository.productSalesByMonth(variantIds, dtFrom, dtTo);
                    }
                };

        // Group rows theo variantId
        // row: [variantId, variantName, label, qty]
        Map<Long, List<Object[]>> groupedByVariant =
                rows.stream().collect(Collectors.groupingBy(r -> ((Number) r[0]).longValue()));

        List<ProductSalesResponse.VariantSales> variants = groupedByVariant.entrySet().stream()
                .map(entry -> {
                    Long variantId = entry.getKey();
                    List<Object[]> variantRows = entry.getValue();

                    String variantName = String.valueOf(variantRows.get(0)[1]);

                    List<ProductSalesResponse.SalesDataPoint> dataPoints = variantRows.stream()
                            .map(r -> ProductSalesResponse.SalesDataPoint.builder()
                                    .label(String.valueOf(r[2]))
                                    .quantitySold(((Number) r[3]).longValue())
                                    .build())
                            .toList();

                    long totalQty = dataPoints.stream()
                            .mapToLong(ProductSalesResponse.SalesDataPoint::getQuantitySold)
                            .sum();

                    return ProductSalesResponse.VariantSales.builder()
                            .variantId(variantId)
                            .variantName(variantName)
                            .totalQuantitySold(totalQty)
                            .dataPoints(dataPoints)
                            .build();
                })
                .sorted(Comparator.comparingLong(ProductSalesResponse.VariantSales::getTotalQuantitySold)
                        .reversed())
                .toList();

        long totalQuantitySold = variants.stream()
                .mapToLong(ProductSalesResponse.VariantSales::getTotalQuantitySold)
                .sum();

        return ProductSalesResponse.builder()
                .productId(productId)
                .period(p.name())
                .totalQuantitySold(totalQuantitySold)
                .variants(variants)
                .build();
    }

    @Override
    public void markOrderDetailReviewed(Long orderDetailId) {

        OrderDetail detail = orderDetailRepository
                .findById(orderDetailId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_DETAIL_NOT_FOUND));

        if (Boolean.TRUE.equals(detail.getReviewed())) {
            return;
        }

        detail.setReviewed(true);
    }

    private String buildContent(Long orderId, String status) {
        switch (status) {
            case "CREATED":
                return "Đơn hàng #" + orderId + " đã được tạo thành công.";
            case "PROCESSING":
                return "Đơn hàng #" + orderId + " đang được xử lý.";
            case "READY_TO_SHIP":
                return "Đơn hàng #" + orderId + " đã sẵn sàng để giao.";
            case "SHIPPING":
                return "Đơn hàng #" + orderId + " đang trên đường giao đến bạn.";
            case "DELIVERED":
                return "Đơn hàng #" + orderId + " đã được giao thành công.";
            case "CANCELLED":
                return "Đơn hàng #" + orderId + " đã bị hủy.";
            case "REFUNDED":
                return "Đơn hàng #" + orderId + " đã được hoàn tiền.";
            case "PARTIALLY_RETURNED":
                return "Đơn hàng #" + orderId + " đã được hoàn trả một phần.";
            default:
                return "Đơn hàng #" + orderId + " có trạng thái: " + status;
        }
    }
}

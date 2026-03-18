package com.techstore.order.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.kafka.core.KafkaTemplate;
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
import com.techstore.order.dto.response.ShippingInfo;
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

    @Override
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

        order.setVat(total * 0.1);
        order.setTotalPrice(total + order.getVat());

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

package com.techstore.order.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.techstore.order.client.ProductServiceClient;
import com.techstore.order.client.WarehouseServiceClient;
import com.techstore.order.dto.response.ApiResponse;
import com.techstore.order.dto.response.VariantInfo;
import com.techstore.order.entity.Order;
import com.techstore.order.entity.OrderDetail;
import com.techstore.order.entity.Refund;
import com.techstore.order.mapper.OrderMapper;
import com.techstore.order.repository.OrderDetailRepository;
import com.techstore.order.repository.OrderRepository;
import com.techstore.order.request.InventoryExportRequest;
import com.techstore.order.request.OrderCreateRequest;
import com.techstore.order.request.OrderItemRequest;
import com.techstore.order.request.OrderResponse;
import com.techstore.order.service.OrderService;
import com.techstore.order.service.payment.PaymentStrategy;
import com.techstore.order.service.payment.PaymentStrategyFactory;

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
    private final OrderMapper mapper;
    private final PaymentStrategyFactory paymentFactory;

    @Override
    public OrderResponse createOrder(OrderCreateRequest request, String ipAddress) {
        log.info("AAAAAAAAAAAAAAAAAAAAAA");
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus("CREATED");

        List<Long> variantIds =
                request.getItems().stream().map(OrderItemRequest::getVariantId).toList();
        log.info("BBBBBBBBBBBBBB");

        ApiResponse<List<VariantInfo>> response = productClient.getVariantsByIds(variantIds);
        log.info("CCCCCCCCCCCCCCCCCCCC");

        List<VariantInfo> variants = response.getResult();

        Map<Long, VariantInfo> variantMap = variants.stream().collect(Collectors.toMap(VariantInfo::getId, v -> v));

        List<OrderDetail> details = new ArrayList<>();
        double total = 0;

        for (OrderItemRequest item : request.getItems()) {

            VariantInfo variant = variantMap.get(item.getVariantId());

            if (variant == null) {
                throw new RuntimeException("Variant not found: " + item.getVariantId());
            }

            OrderDetail detail = OrderDetail.builder()
                    .variantId(item.getVariantId())
                    .quantity(item.getQuantity().intValue())
                    .price(variant.getPrice())
                    .status("ACTIVE")
                    .order(order)
                    .build();

            total += detail.getPrice() * detail.getQuantity();
            details.add(detail);
        }

        order.setTotalPrice(total);
        order.setVat(total * 0.1);
        order.setOrderDetails(details);

        orderRepository.save(order);
        log.info("DDDDDDDDDDDDDDD");

        String paymentUrl = null;

        if (request.getPaymentMethod() != null) {

            PaymentStrategy strategy = paymentFactory.getStrategy(request.getPaymentMethod());

            paymentUrl = strategy.createPaymentUrl(order, ipAddress);
        }
        log.info("EEEEEEEEEEEEEEEEE");

        warehouseClient.exportInventory(InventoryExportRequest.builder()
                .orderId(order.getId())
                .items(request.getItems())
                .build());
        log.info("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

        OrderResponse result = mapper.toDto(order);
        result.setPaymentUrl(paymentUrl);
        return result;
    }

    @Override
    public void confirmOrder(Long orderId, Long staffId) {

        Order order = orderRepository.findById(orderId).orElseThrow();

        order.setStaffId(staffId);
        order.setStatus("READY_TO_SHIP");
    }

    @Override
    public void cancelOrder(Long orderId, Long staffId) {

        Order order = orderRepository.findById(orderId).orElseThrow();

        order.setStaffId(staffId);
        order.setStatus("CANCELLED");

        // rollback warehouse
        warehouseClient.cancelTransaction(order.getWarehouseTransactionId());

        // refund payment
        if (order.getPayment() != null) {
            order.getPayment().setStatus("REFUNDED");
        }
    }

    @Override
    public void updateStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
    }

    @Override
    public void createRefund(Long detailId, String reason, Long staffId) {

        OrderDetail detail = orderDetailRepository.findById(detailId).orElseThrow();

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
    }
}

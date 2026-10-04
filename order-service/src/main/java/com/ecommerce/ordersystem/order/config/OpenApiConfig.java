package com.ecommerce.ordersystem.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("""
                                ### 🚀 Quick Testing Journey: View Order ➔ Track Order ➔ Advance ➔ Cancel
                                
                                All endpoints below come pre-populated with **working sample inputs** for 1-click execution:
                                
                                1. **Step 1: View Pre-Seeded Orders**
                                   - Open `GET /api/orders` ➔ Click *Try it out* ➔ *Execute*.
                                   - Inspect the initialized demo order `ORD-DEMO-001`.
                                
                                2. **Step 2: Track Order by ID**
                                   - Open `GET /api/orders/{orderId}` ➔ Pre-filled with `ORD-DEMO-001` ➔ *Execute*.
                                   - Observe the current order status (`PENDING`), item details, and total amount.
                                
                                3. **Step 3: Create a New Order**
                                   - Open `POST /api/orders` ➔ Click *Try it out* (pre-loaded with items `ITEM-101` and `ITEM-102`) ➔ *Execute*.
                                   - Copy the generated order ID to track it.
                                
                                4. **Step 4: Advance & Track Status Transitions**
                                   - Open `PUT /api/orders/{orderId}/status` ➔ Select *Advance to SHIPPED* example ➔ *Execute*.
                                   - Call `GET /api/orders/{orderId}` again to verify the tracked status updated to `SHIPPED`.
                                
                                5. **Step 5: Test Cancellation Rules**
                                   - Open `POST /api/orders/{orderId}/cancel` on a `PENDING` order ➔ *Execute* (Restores stock to inventory).
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("E-Commerce Architecture Team"))
                        .license(new License().name("Apache 2.0")));
    }
}

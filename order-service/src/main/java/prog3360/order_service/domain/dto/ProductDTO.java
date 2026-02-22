package prog3360.order_service.domain.dto;

public class ProductDTO {
        private Long id;
        private String name;
        private double price;
        private int quantity;

        public Long getId(){
            return id;
        }

        public String getName(){
            return name;
        }

        public int getQuantity(){
            return quantity;
        }

        public double getPrice(){
            return price;
        }
}

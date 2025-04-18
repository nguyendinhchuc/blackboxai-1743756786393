# Cấu Trúc Dự Án E-commerce API

## Giới Thiệu
Đây là dự án API thương mại điện tử được xây dựng bằng Java Spring Boot, sử dụng Maven để quản lý dependency và Docker để container hóa. Dự án được tổ chức theo mô hình phân lớp rõ ràng, đảm bảo tính module hóa và dễ bảo trì.

## Cấu Trúc Thư Mục Chính
```
blackboxai-1743756786393/
├── pom.xml                 # File cấu hình Maven
├── Dockerfile             # Cấu hình Docker
├── docker-compose.yml     # Cấu hình Docker Compose
├── deploy.sh             # Script triển khai
├── sql/                  # Thư mục chứa scripts SQL
└── src/                  # Mã nguồn chính
```

## Chi Tiết Các Package Trong src/main/java/com/ecommerce/api

### 1. Config (Cấu hình)
Chứa các file cấu hình của ứng dụng:
- `SecurityConfig.java`: Cấu hình bảo mật
- `JwtConfig.java`: Cấu hình JWT
- `CorsConfig.java`: Cấu hình CORS
- `WebSocketConfig.java`: Cấu hình WebSocket
- Và các file cấu hình khác...

### 2. Controller (Điều khiển)
Xử lý các request HTTP:
- `AuthController.java`: Xử lý đăng nhập/đăng ký
- `ProductController.java`: Quản lý sản phẩm
- `CategoryController.java`: Quản lý danh mục
- `PaymentController.java`: Xử lý thanh toán
- Package `admin/`: Chứa các controller cho trang quản trị

### 3. Model (Mô hình dữ liệu)
Các entity chính của hệ thống:
- `User.java`: Thông tin người dùng
- `Product.java`: Thông tin sản phẩm
- `Category.java`: Danh mục sản phẩm
- `Order.java`: Đơn hàng
- `Payment.java`: Thanh toán

### 4. Service (Dịch vụ)
Xử lý logic nghiệp vụ:
- `AuthService.java`: Dịch vụ xác thực
- `ProductService.java`: Quản lý sản phẩm
- `UserDetailsServiceImpl.java`: Triển khai chi tiết người dùng
- `PaymentService.java`: Xử lý thanh toán

### 5. Repository (Kho dữ liệu)
Tương tác với cơ sở dữ liệu:
- `UserRepository.java`: Thao tác với bảng users
- `ProductRepository.java`: Thao tác với bảng products
- `CategoryRepository.java`: Thao tác với bảng categories

### 6. Security (Bảo mật)
Xử lý vấn đề bảo mật:
- `JwtTokenProvider.java`: Tạo và xác thực JWT
- `JwtAuthenticationFilter.java`: Filter xác thực JWT
- `CustomAuthenticationSuccessHandler.java`: Xử lý đăng nhập thành công

### 7. Exception (Xử lý ngoại lệ)
Quản lý lỗi trong ứng dụng:
- `CustomException.java`: Định nghĩa ngoại lệ tùy chỉnh
- `GlobalExceptionHandler.java`: Xử lý ngoại lệ toàn cục

### 8. DTO (Data Transfer Objects)
Đối tượng truyền dữ liệu:
- `BaseDTO.java`: DTO cơ sở
- `RevisionDTO.java`: DTO cho phiên bản

### 9. Aspect (Các khía cạnh)
Xử lý các vấn đề cắt ngang:
- `RevisionAspect.java`: Ghi log thay đổi
- `RevisionNotificationAspect.java`: Thông báo thay đổi

### 10. Cache (Bộ nhớ đệm)
Quản lý cache:
- `RevisionCache.java`: Cache cho phiên bản
- `RevisionNotificationCache.java`: Cache cho thông báo

### 11. Event (Sự kiện)
Xử lý các sự kiện:
- `RevisionEvent.java`: Sự kiện phiên bản
- `RevisionEventListener.java`: Lắng nghe sự kiện
- `RevisionPublisher.java`: Phát sự kiện

### 12. Metrics (Đo lường)
Thu thập số liệu:
- `RevisionMetrics.java`: Đo lường phiên bản
- `RevisionNotificationMetrics.java`: Đo lường thông báo

### 13. Validator (Kiểm tra)
Kiểm tra dữ liệu:
- `BaseValidator.java`: Validator cơ sở
- `RevisionValidator.java`: Kiểm tra phiên bản

### 14. Util (Tiện ích)
Các công cụ hỗ trợ:
- `RevisionUtils.java`: Công cụ xử lý phiên bản
- `TenantContext.java`: Context cho đa người thuê

## Cấu Trúc Resources
```
src/main/resources/
├── application.properties    # Cấu hình ứng dụng
├── application.yml          # Cấu hình YAML
└── templates/               # Templates HTML
    ├── admin/              # Giao diện admin
    └── notification/       # Templates thông báo
```

## Các Tính Năng Chính
1. Xác thực và phân quyền với JWT
2. Quản lý sản phẩm và danh mục
3. Xử lý đơn hàng và thanh toán
4. Hệ thống thông báo
5. Dashboard quản trị
6. Theo dõi và đo lường
7. Cache để tối ưu hiệu suất
8. Xử lý đa người thuê (Multi-tenancy)

## Quy Tắc Phát Triển
1. Tuân thủ nguyên tắc SOLID
2. Sử dụng dependency injection
3. Xử lý lỗi tập trung
4. Ghi log đầy đủ
5. Kiểm tra dữ liệu chặt chẽ
6. Tối ưu hiệu suất với cache
7. Đảm bảo bảo mật

## Hướng Dẫn Phát Triển
1. Tạo branch mới cho mỗi tính năng
2. Viết unit test đầy đủ
3. Tuân thủ coding style
4. Cập nhật tài liệu khi thêm tính năng mới
5. Review code trước khi merge

## Triển Khai
1. Sử dụng Docker để container hóa
2. Cấu hình CI/CD với deploy.sh
3. Quản lý phiên bản với Git
4. Monitoring với metrics

## Kết Luận
Dự án được tổ chức theo cấu trúc rõ ràng, module hóa cao, dễ mở rộng và bảo trì. Việc tuân thủ các quy tắc và hướng dẫn phát triển sẽ giúp duy trì chất lượng code và hiệu suất của hệ thống.

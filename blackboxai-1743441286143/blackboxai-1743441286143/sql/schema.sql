-- Tạo bảng roles
CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name ENUM('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN') NOT NULL
);

-- Tạo bảng users
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(20) NOT NULL UNIQUE,
  email VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(120) NOT NULL
);

-- Tạo bảng user_roles (quan hệ many-to-many giữa users và roles)
CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Tạo bảng categories
CREATE TABLE categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  parent_id BIGINT,
  image_url VARCHAR(255),
  FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- Tạo bảng products
CREATE TABLE products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  stock_quantity INT,
  category_id BIGINT,
  FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Tạo bảng product_images
CREATE TABLE product_images (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  image_url VARCHAR(255) NOT NULL,
  product_id BIGINT NOT NULL,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Tạo bảng payments
CREATE TABLE payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(50) NOT NULL,
  transaction_id VARCHAR(100) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL,
  payment_date DATETIME NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tạo bảng tenants (quản lý các domain/subdomain)
CREATE TABLE tenants (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  domain VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Thêm cột tenant_id vào các bảng cần phân tách dữ liệu
ALTER TABLE users ADD COLUMN tenant_id BIGINT;
ALTER TABLE categories ADD COLUMN tenant_id BIGINT;
ALTER TABLE products ADD COLUMN tenant_id BIGINT;
ALTER TABLE payments ADD COLUMN tenant_id BIGINT;

-- Thêm ràng buộc khóa ngoại
ALTER TABLE users ADD CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE categories ADD CONSTRAINT fk_category_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE products ADD CONSTRAINT fk_product_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);
ALTER TABLE payments ADD CONSTRAINT fk_payment_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Tạo bảng banners
CREATE TABLE banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    tenant_id BIGINT,
    start_date DATETIME,
    end_date DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Thêm dữ liệu mẫu
INSERT INTO roles (name) VALUES 
('ROLE_USER'),
('ROLE_MODERATOR'),
('ROLE_ADMIN');

INSERT INTO tenants (domain, name, description) VALUES
('main.example.com', 'Main Store', 'Primary e-commerce store'),
('fashion.example.com', 'Fashion Store', 'Specialized in fashion products'),
('electronics.example.com', 'Electronics Store', 'Specialized in electronic devices');

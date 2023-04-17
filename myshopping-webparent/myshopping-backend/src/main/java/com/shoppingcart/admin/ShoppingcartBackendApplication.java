package com.shoppingcart.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication//@SpringBootApplication = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan -->nhờ 3 annotation này mà Spring Boot có thể tự cấu hình, nếu sử dụng SpringMVC thì phải tự cấu hình
@ComponentScan({ "com.shoppingcart.admin.*", "com.shoppingcart.admin" })//quét qua các package được khai báo để khởi tạo Spring Bean. Muốn khởi tạo Spring Bean ở cấp độ class dùng @Controller, @Service, @Repository, @Component. Muốn khởi tạo Spring Bean ở cấp độ phương thức dùng @Configuration + @Bean
@EnableJpaRepositories(basePackages = { "com.shoppingcart.admin.*" })//quét qua các package được khai báo để khởi tạo Spring Data JPA
@EntityScan({ "com.shoppingcart.common.*" })//quét qua các package được khai báo để khởi tạo entity -->sẽ tạo table tương ứng trong database
public class ShoppingcartBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoppingcartBackendApplication.class, args);//chạy app
	}
	
/*
Deploy Shopping Cart Admin App on to Heroku

1. Tạo tài khoản heroku -->https://signup.heroku.com/
2. Sau khi tạo tài khoản xong thì nhấn Profile > Account settings > Billing > nhập số thẻ VISA
3. Download -->https://devcenter.heroku.com/articles/heroku-cli
4. Cài đặt

5. Tạo 2 file có tên và nội dung như sau vào folder myshopping-project

5.1/ system.properties
java.runtime.version=11
-->dùng để xác định version để chạy app

5.2/ Procfile
web: java -Dserver.port=$PORT -jar $PATH_TO_JAR
-->dùng để chỉ định cách heroku chạy app -->đây là app web và chạy bằng câu lệnh java -jar $PATH_TO_JAR

6. Sửa lại nội dung của file application.properties
comment context path
thêm 2 dòng sau:
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.maximum-pool-size=2


7. Mở cmd tại myshopping-project -->C:\Users\Tuyen Nguyen\Downloads\nhbtuyen2702\myshopping-project
8. chạy lệnh: heroku login -->xuất hiện trình duyệt để login -->nhập email và password để login -->sau khi login thành công quay lại cmd sẽ thấy báo success
9. chạy lệnh: heroku status -->dùng để kiểm tra Apps, Data, Tools
10. chạy lệnh: mvn clean -->xóa toàn bộ folder target trong tất cả project -->5 project sẽ bị xóa folder target
11. chạy lệnh: mvn install -->dùng để biên dịch, đóng gói và cài đặt các module và các dependency, nó sẽ tìm kiếm trong local repository xem đã có các module và dependency này chưa, nếu chưa có thì nó tải từ remote repository(internet) -->khi chạy thành công lệnh này có thể vào các folder target để kiểm tra các gói jar được tạo ra
Lưu ý: thêm plugin sau vào pom.xml

<build>
	<plugins>
		<plugin>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-maven-plugin</artifactId>
		</plugin>
	</plugins>
</build>

12. Mở cmd tại folder target trong myshopping-backend -->C:\Users\Tuyen Nguyen\Downloads\nhbtuyen2702\myshopping-project\myshopping-webparent\myshopping-backend\target
13. chạy lệnh: java -jar myshopping-backend-0.0.1-SNAPSHOT.jar -->myshopping-backend đang được chạy

14. Mở cmd tại folder target trong myshopping-project -->C:\Users\Tuyen Nguyen\Downloads\nhbtuyen2702\myshopping-project
15. chạy lệnh: heroku create myshopping-backend -->dùng để tạo app myshopping-backend trong heroku
Sau khi tạo app thành công thì sẽ thấy 2 đường dẫn như sau
https://myshopping-backend.herokuapp.com -->dùng để truy cập app myshopping-backend trong browser 
https://git.heroku.com/myshopping-backend.git -->đường dẫn chứa source code của myshopping-backend trong heroku

16. chạy lệnh: git remote -v -->sẽ thấy các remote đang có trong myshopping-backend
17. chạy lệnh: git push heroku master -->đẩy source code vào remote heroku nhánh master
18. chạy lệnh: heroku apps -->xuất hiện app myshopping-backend, có thể lên browser để xem

19. chạy lệnh: heroku config -->để xem tất cả các biến môi trường -->cần phải gán giá trị cho biến $PATH_TO_JAR
20. chạy lệnh: heroku config:set PATH_TO_JAR=myshopping-webparent/myshopping-backend/target/myshopping-backend-0.0.1-SNAPSHOT.jar -->gán giá trị cho biến PATH_TO_JAR trỏ tới đường dẫn chứa jar file
21. chạy lệnh: heroku config -->sẽ thấy biến môi trường PATH_TO_JAR
22. chạy lệnh: heroku open -->dùng để mở app trong browser -->sẽ bị lỗi vì chưa có kết nối tới MySQL
23. chạy lệnh: heroku logs --tail -->dùng để kiểm tra log của app

24. chạy lệnh: heroku addons:create cleardb:ignite -a myshopping-backend -->dùng để tạo một addon(phần mềm bổ sung) có tên là cleardb:ignite vào trong app có tên là myshopping-backend(-a để khai báo tên app), addon này cung cấp cơ sở dữ liệu MySQL cho app, khi chạy xong lệnh này heroku sẽ tạo một database mới cung cấp cho app myshopping-backend
25. chạy lệnh: heroku addons -->xem tất cả addons đang có của app
(có thể xem nhiều addons khác tại đây: https://elements.heroku.com/search/addons?q=mysql)
26. chạy lệnh: heroku run -a myshopping-backend printenv
Giải thích:  
dyno đại diện cho môi trường chạy app trên nền tảng heroku, mỗi dyno là một máy ảo riêng biệt gồm có CPU, RAM, bộ nhớ đệm, bộ nhớ lưu trữ.
-->heroku run được dùng để chạy một lệnh nào đó trên dyno, heroku sẽ tạo một dyno mới và chạy lệnh trên dyno mới này, sau khi chạy xong dyno này sẽ bị hủy
Trong trường hợp này đang chạy lệnh printenv trên dyno để hiển thị các biến môi trường trên dyno
-a myshopping-backend là tên của app

Lưu ý các thông tin sau để kết nối đến database
JDBC_DATABASE_URL=jdbc:mysql://us-cdbr-east-06.cleardb.net/heroku_9daef5f96cbc321?password=0359428b&reconnect=true&user=ba728885230bb8
JDBC_DATABASE_USERNAME=ba728885230bb8
JDBC_DATABASE_PASSWORD=0359428b
-->dùng Workbench để nối tới ClearDB này

27. Sau khi kết nối thành công bằng Workbench, vào database cũ export file sql
Chọn myshopping > Server > Data Export > chọn myshop, nhấn double click vào myshop > chọn Export to Self-Contained File > chọn Start Export
Thêm dòng này vào đầu file vừa export: use heroku_9daef5f96cbc321;
Sau khi export xong thì vào Workbench đã kế nối tới heroku chọn database heroku_9daef5f96cbc321 > Server > Data Import > chọn Import from Self-Contained File > nhấn vào dấu ... để chọn file vừa export > Start Import
Sau khi import thành công thì vào brower truy cập đường dẫn https://myshopping-backend.herokuapp.com -->Done

*/

}

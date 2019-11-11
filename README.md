 # Broadlink Bridge
 - [Introductions](#introdution)
 - [Requirements](#requirements)
 - [Installation](#installation)
 ## Introductions
 Một project được phát triển từ [Broadlink MQTT Bridge](https://github.com/fbacker/broadlink-mqtt-bridge) để thực hiện giao tiếp, quản lý các thiết bị broadlink như `Broadlink RM Mini 3`, `Broadlink RM3 Pro Plus`, ... bao gồm các chức năng: 
 - Tìm kiếm thiết bị broadlink trong mạng
 - Hiển thị danh sách thiết bị broadlink đã kết nối
 - Điều khiển Broadlink học lệnh IR/RF
 - Hiển thị các lệnh đã học
 - Thực hiện chạy lệnh đã học
 
 Thông qua các cách giao tiếp sau:
 - MQTT: Thực hiện Giao tiếp với thiết bị Broadlink qua việc publish/subscriber tới các topic (sau này có thể sử dụng topic là đường link cho openHAB)
 - Web server: Giao tiếp với thiết bị Broadlink qua giao diện người dùng
 - Rest API: Giao tiếp với thiết bị Broadlink qua HTTP request tới các API
 ## Requirements

- [Nodejs](https://nodejs.org/en/) > 8 
- MQTT ([mosquitto](https://mosquitto.org/) hoặc các MQTT broker khác )
- Thiết bị Broadlink e.g. RM 3 mini 3 ...

## Installation
```sh
npm install 
```

## Configure
Cấu hình được đặt trong `./config/default.json`, bạn có thể thay đổi cấu hình mqtt, web server và cấu hình thiết bị broadlink

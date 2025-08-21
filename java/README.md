# iTick.org 股票 API 客户端

基于 Spring Boot 3 的 iTick.org 股票 API 客户端，支持：
- 通过 WebSocket 订阅实时股票报价数据
- 通过 HTTP 请求获取 K 线（蜡烛图）数据

## 功能特点

- 通过 WebSocket 实时订阅股票数据
- 支持不同类型的数据：报价(quotes)、成交(ticks)、盘口(depth)和 K 线(K-lines)
- 用于获取历史 K 线数据的 HTTP 客户端
- 自动重连和心跳机制
- 提供 RESTful API 接口

## 系统要求

- Java 17 或更高版本
- Maven 3.6 或更高版本

## 配置说明

编辑 `application.properties` 文件来配置您的 API 令牌和其他设置：

```properties
# API 配置
itick.api.base-url=https://api.itick.org
itick.api.websocket-url=wss://api.itick.org/stock
itick.api.token=your-api-token-here
```

## 构建应用

```bash
mvn clean package
```

## 运行应用

```bash
java -jar target/java-client-0.0.1-SNAPSHOT.jar
```

或使用 Maven：

```bash
mvn spring-boot:run
```

## API 使用说明

### HTTP 接口

#### 获取 K 线数据

```
GET /api/kline?region={region}&code={code}&kType={kType}&endTime={endTime}&limit={limit}
```

参数说明：
- `region`：市场代码（必填）
- `code`：产品代码（必填）
- `kType`：周期类型（必填）
  - 1：1分钟
  - 2：5分钟
  - 3：10分钟
  - 4：30分钟
  - 5：1小时
  - 6：2小时
  - 7：4小时
  - 8：1天
  - 9：1周
  - 10：1月
- `endTime`：结束时间（可选）
- `limit`：返回记录数量（可选）

#### 订阅实时数据

```
POST /api/subscribe?symbols={symbols}&types={types}
```

参数说明：
- `symbols`：以逗号分隔的股票代码列表（例如："ETHUSDT"）
- `types`：以逗号分隔的数据类型列表（例如："quote,depth,tick"）

## WebSocket 数据类型

客户端支持以下 WebSocket 数据类型：

- `quote`：实时报价，包括开盘价、最高价、最低价、最新价等
- `tick`：实时成交数据
- `depth`：盘口数据（买单和卖单）

## 使用示例

订阅数据事件，使用 `WebSocketClientService`：

```java
@Service
@RequiredArgsConstructor
public class YourService {

    private final WebSocketClientService webSocketClientService;

    public void initialize() {
        // 订阅数据事件
        webSocketClientService.subscribeToQuotes(quoteData -> {
            // 处理报价数据
            System.out.println("收到 " + quoteData.getS() + " 的报价: " + quoteData.getLd());
        });
        
        // 订阅特定产品
        webSocketClientService.subscribe("BTCUSDT,ETHUSDT", "quote,depth");
    }
}
```

获取 K 线数据，使用 `KlineService`：

```java
@Service
@RequiredArgsConstructor
public class YourService {

    private final KlineService klineService;

    public void fetchKlineData() {
        klineService.getKlineData("US", "ETHUSDT", 8, null, 100)
            .subscribe(klineDataList -> {
                // 处理 K 线数据
                klineDataList.forEach(kline -> {
                    System.out.println("开盘价: " + kline.getO() + ", 收盘价: " + kline.getC());
                });
            });
    }
}
```

## 许可证

本项目采用 MIT 许可证 - 详情请参阅 LICENSE 文件

// 导入必要的模块
const WebSocket = require('ws'); // WebSocket客户端模块
const winston = require('winston'); // 日志记录模块

// 配置日志记录器
const logger = winston.createLogger({
    level: 'info', // 设置日志级别为info
    format: winston.format.combine(
        // 添加时间戳，格式为：年-月-日 时:分:秒.毫秒
        winston.format.timestamp({
            format: 'YYYY-MM-DD HH:mm:ss.SSS'
        }),
        // 自定义日志输出格式
        winston.format.printf(({ timestamp, level, message }) => {
            return `${timestamp} ${level}: ${message}`;
        })
    ),
    // 配置日志输出目标：同时输出到控制台和文件
    transports: [
        new winston.transports.Console(), // 输出到控制台
        new winston.transports.File({ filename: 'gold-price.log' }) // 输出到文件
    ]
});

/**
 * 黄金价格订阅器类
 * 用于连接WebSocket服务器，订阅黄金价格数据，并处理自动重连
 */
class GoldPriceSubscriber {
    constructor() {
        // WebSocket服务器地址
        this.wsUrl = 'wss://api.itick.org/fws';
        // 认证令牌
        this.authToken = 'a5ca43babf5e49c4b734bdcb6f51a4a4465d52bd3fbe48e1847ac9259ae290c8';
        // 订阅的交易对符号
        this.symbol = 'XAUUSD';
        // WebSocket连接实例
        this.ws = null;
        // 连接状态标志
        this.isConnected = false;
        // 重连尝试次数
        this.reconnectAttempts = 0;
        // 最大重连尝试次数
        this.maxReconnectAttempts = 100;
    }

    /**
     * 建立WebSocket连接
     * @returns {Promise} 返回连接成功或失败的Promise
     */
    connect() {
        return new Promise((resolve, reject) => {
            try {
                // 创建新的WebSocket连接
                this.ws = new WebSocket(this.wsUrl);

                // 监听连接打开事件
                this.ws.on('open', () => {
                    logger.info('已成功连接到WebSocket服务器');
                    this.isConnected = true;
                    this.reconnectAttempts = 0; // 重置重连计数器
                    resolve(true);
                });

                // 监听数据接收事件
                this.ws.on('message', (data) => {
                    // 使用上海时区记录接收时间
                    const timestamp = new Date().toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' });
                    logger.info(`[${timestamp}] 收到数据: ${data}`);
                });

                // 监听连接关闭事件
                this.ws.on('close', async () => {
                    logger.warn('WebSocket连接已关闭');
                    this.isConnected = false;
                    await this.handleReconnection(); // 触发重连机制
                });

                // 监听错误事件
                this.ws.on('error', (error) => {
                    logger.error(`WebSocket错误: ${error.message}`);
                    this.isConnected = false;
                });

            } catch (error) {
                logger.error(`连接错误: ${error.message}`);
                reject(error);
            }
        });
    }

    /**
     * 发送认证信息
     * @returns {Promise<boolean>} 返回认证是否成功
     */
    async authenticate() {
        if (!this.isConnected) {
            logger.error('无法认证: 未连接到服务器');
            return false;
        }

        try {
            // 构建认证消息
            const authMessage = {
                ac: 'auth',
                params: this.authToken
            };
            
            this.ws.send(JSON.stringify(authMessage));
            logger.info('已发送认证请求');
            return true;
        } catch (error) {
            logger.error(`认证错误: ${error.message}`);
            return false;
        }
    }

    /**
     * 订阅行情数据
     * @returns {Promise<boolean>} 返回订阅是否成功
     */
    async subscribe() {
        if (!this.isConnected) {
            logger.error('无法订阅: 未连接到服务器');
            return false;
        }

        try {
            // 构建订阅消息
            const subscribeMessage = {
                ac: 'subscribe',
                params: this.symbol,
                types: 'tick'
            };
            
            this.ws.send(JSON.stringify(subscribeMessage));
            logger.info(`已订阅 ${this.symbol} 行情数据`);
            return true;
        } catch (error) {
            logger.error(`订阅错误: ${error.message}`);
            return false;
        }
    }

    /**
     * 处理断线重连
     * 包含延迟重试和最大重试次数限制
     */
    async handleReconnection() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            logger.error('已达到最大重连次数');
            return;
        }

        this.reconnectAttempts++;
        logger.info(`正在尝试重新连接 (第 ${this.reconnectAttempts} 次尝试)`);

        try {
            // 等待5秒后重连
            await new Promise(resolve => setTimeout(resolve, 5000));
            await this.start();
        } catch (error) {
            logger.error(`重连失败: ${error.message}`);
        }
    }

    /**
     * 启动订阅服务
     * 按顺序执行：连接 -> 认证 -> 订阅
     */
    async start() {
        try {
            await this.connect();
            if (this.isConnected) {
                await this.authenticate();
                await this.subscribe();
            }
        } catch (error) {
            logger.error(`启动失败: ${error.message}`);
            await this.handleReconnection();
        }
    }
}

// 创建并启动订阅器实例
const subscriber = new GoldPriceSubscriber();
subscriber.start().catch(error => {
    logger.error(`程序错误: ${error.message}`);
});

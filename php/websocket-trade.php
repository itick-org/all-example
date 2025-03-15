<?php
require 'vendor/autoload.php';

use Ratchet\Client\WebSocket;
use React\EventLoop\Loop;

// 加载配置
$config = [
    'api_key' => getenv('ITICK_API_KEY') ?: 'api_key', // 建议使用环境变量
    'websocket_url' => 'wss://api.itick.org/fws',
    'symbol' => 'EURUSD',
    'types' => 'depth,quote'
];

// WebSocket服务器的地址
$websocket_url = $config['websocket_url'];

// 用于鉴权的消息
$auth_message = [
    "ac" => "auth",
    "params" => $config['api_key']
];

// 用于订阅的消息格式
$subscribe_message = [
    "ac" => "subscribe",
    "params" => $config['symbol'],
    "types" => $config['types']
];

// 创建事件循环
$loop = Loop::get();

// 设置错误处理
set_error_handler(function($errno, $errstr, $errfile, $errline) {
    echo "PHP Error: [$errno] $errstr in $errfile on line $errline\n";
});

try {
    \Ratchet\Client\connect($websocket_url)->then(function(WebSocket $conn) use ($auth_message, $subscribe_message) {
        // 连接成功时的处理
        echo "WebSocket连接已打开，正在发送鉴权消息...\n";
        
        // 发送鉴权消息
        $conn->send(json_encode($auth_message));
        
        // 等待一小段时间后发送订阅消息
        Loop::addTimer(1.0, function() use ($conn, $subscribe_message) {
            echo "发送订阅消息...\n";
            $conn->send(json_encode($subscribe_message));
        });
        
        // 处理接收到的消息
        $conn->on('message', function($msg) {
            echo "收到消息: " . $msg . "\n";
            
            // 解析JSON数据
            $data = json_decode($msg, true);
            if (json_last_error() !== JSON_ERROR_NONE) {
                echo "JSON解析错误: " . json_last_error_msg() . "\n";
                return;
            }
            
            if (isset($data['data'])) {
                echo "数据内容: " . json_encode($data['data'], JSON_PRETTY_PRINT) . "\n";
            }
        });
        
        // 处理错误
        $conn->on('error', function($error) {
            echo "WebSocket错误: " . $error->getMessage() . "\n";
        });
        
        // 处理连接关闭
        $conn->on('close', function($code = null, $reason = null) {
            echo "WebSocket连接已关闭，状态码: " . $code . "，消息: " . $reason . "\n";
        });
        
    }, function($e) {
        // 连接失败时的处理
        echo "无法连接到WebSocket服务器: " . $e->getMessage() . "\n";
    });

    // 运行事件循环
    $loop->run();
    
} catch (\Exception $e) {
    echo "发生异常: " . $e->getMessage() . "\n";
} finally {
    // 恢复默认的错误处理
    restore_error_handler();
}
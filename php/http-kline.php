<?php

// 设置API请求URL
$url = "https://api.itick.org/stock/tick?region=HK&code=700";

// 设置请求头
$headers = array(
    "accept: application/json",
    "token: api_key"
);

// 初始化CURL
$ch = curl_init();

// 设置CURL选项
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

// 执行请求
$response = curl_exec($ch);

// 检查是否有错误发生
if(curl_errno($ch)) {
    echo '请求错误: ' . curl_error($ch);
}

// 关闭CURL连接
curl_close($ch);

// 输出响应结果
echo $response;
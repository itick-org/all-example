# 使用说明


# 1、首先确保已安装 Homebrew（如果没有安装）：

``` bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

# 2、使用 Homebrew 安装 Composer：
``` bash
brew install composer
```

# 3、验证安装：
``` bash
composer --version
```

# 4、在你的项目目录中初始化 Composer：
``` bash
# 进入项目目录
cd 你的项目目录

# 初始化 composer.json
composer init --no-interaction

# 安装需要的依赖
composer require ratchet/pawl react/event-loop
```

# 5、如果你不想使用 Homebrew，也可以手动安装 Composer：
``` bash
# 下载安装脚本
php -r "copy('https://getcomposer.org/installer', 'composer-setup.php');"

# 验证安装程序
php -r "if (hash_file('sha384', 'composer-setup.php') === 'api_key') { echo 'Installer verified'; } else { echo 'Installer corrupt'; unlink('composer-setup.php'); } echo PHP_EOL;"

# 运行安装程序
php composer-setup.php

# 删除安装程序
php -r "unlink('composer-setup.php');"

# 移动到全局位置
sudo mv composer.phar /usr/local/bin/composer
```

# 6、安装完成后，你需要：
在项目目录中创建 composer.json 文件：
``` bash
{
    "require": {
        "ratchet/pawl": "^0.4.1",
        "react/event-loop": "^1.3"
    }
}


# 7、安装依赖：
``` bash
composer install
```

# 8、在终端中运行：
``` bash
php http-kline.php

php websocket-trade.php
```

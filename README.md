# ZQLogin
### 介绍
本插件是一个简单的登录插件!  
注意：此插件需要前置插件—`ZQExtension.jar`
### 功能
- 包含玩家注册、登录、黑名单、白名单等功能
- 简单易用，无需使用命令登录
- 未来计划增加邮箱绑定功能，并GUI化
### 配置文件
#### "check-update"
- 插件自动更新开关,"true"或"false"
#### "login-timeout"
- 登录超时时间,单位:秒
#### "bind-cid"
- 绑定cid开关,防恶意注册和非法登陆
- 仅sqlserver"和"mysql"模式下可用,本地模式请勿启动本功能
#### "using_MD5和salt"
- 密码是否使用MD5 + salt加密,salt可不填
### 指令
- `/changepassword <string: new password>` 更换新的登录密码命令
### 权限
		login.command:
		children:
		login.command.changepassword:
		default: op
		login.command.bindemail:
		default: op
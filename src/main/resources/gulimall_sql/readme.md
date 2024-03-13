1、gulimall_*.sql 文件均为独立的建表语句，单独执行即可。

2、pms_catelog.sql是配合gulimall_pms使用的，负责给pms插入数据。

3、renrenfast.sql是renren框架提供的，执行后再执行sys_menus.sql，这个是尚硅谷提供的初始化数据。
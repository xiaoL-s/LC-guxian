# 固贤纱窗厂 ERP 管理系统

> 面向纱窗厂内部员工使用的生产管理系统：从客户下单、尺寸公式自动算料、生产派工扫码报工、计件工资到打印生产单全流程闭环。
> 技术栈：Java 17 + Spring Boot + Spring Cloud（微服务） / Vue 3 + TypeScript + Element Plus / 微信小程序（工人端）。

---

## 一、项目结构

```text
guxian/
├── guxian-erp-parent/              # 后端：Spring Boot 微服务父工程
│   ├── pom.xml                     # 父 POM：统一依赖版本管理
│   ├── compose.yaml                # 本地开发一键启动编排（Nacos/MySQL/Redis）
│   │
│   ├── guxian-gateway/             # 网关：路由转发、跨域统一入口
│   ├── guxian-system/              # 系统模块：登录认证、字典管理、产品公式引擎
│   │   └── src/main/java/com/guxian/system/
│   │       ├── controller/        # 字典/用户/打印模板等接口
│   │       ├── formula/           # 公式引擎（公式解析、尺寸/剪网计算）
│   │       ├── entity/mapper/      # 数据层
│   │       └── service/            # 业务层
│   ├── guxian-customer/            # 客户管理：客户档案、跟进记录
│   ├── guxian-sales/               # 销售订单：下单、订单明细、审核
│   ├── guxian-produce/            # 生产模块：工单、工序流转、扫码报工、计件工资、入库
│   ├── guxian-stock/              # 库存货架：成品上架、库位管理
│   ├── guxian-finance/             # 财务模块（预留扩展）
│   ├── guxian-purchase/            # 采购模块（预留扩展）
│   ├── guxian-supplier/            # 供应商模块（预留扩展）
│   ├── guxian-report/             # 报表模块（预留扩展）
│   ├── guxian-mini-api/           # 工人端小程序后端：任务列表、扫码报工转发、我的工资
│   │
│   ├── guxian-common/              # 公共模块（各业务模块依赖）
│   │   ├── guxian-common-core/    # 通用工具类、统一返回结果、异常
│   │   ├── guxian-common-security/ # Spring Security + JWT 认证鉴权
│   │   ├── guxian-common-mybatis/  # MyBatis-Plus 分页、多租户拦截器
│   │   ├── guxian-common-redis/    # Redis 缓存
│   │   └── guxian-common-file/     # 文件上传服务
│   │
│   └── sql/                        # 建表脚本与初始化 SQL
│
├── guxian-erp-screen/              # 前端：员工后台（Vue 3 + TS + Element Plus）
│   ├── src/
│   │   ├── api/                    # 接口封装（按模块拆分）
│   │   │   ├── customer/           # 客户接口
│   │   │   ├── sales/              # 销售订单接口
│   │   │   ├── production/         # 生产工单/报工接口
│   │   │   ├── stock/              # 库存接口
│   │   │   └── system/             # 字典/用户/打印模板接口
│   │   ├── views/                  # 页面（按业务模块组织）
│   │   │   ├── login/              # 登录
│   │   │   ├── system/             # 系统管理：用户、字典、打印模板设计器
│   │   │   ├── customer/            # 客户档案、跟进记录
│   │   │   ├── sales/              # 销售订单下单/编辑
│   │   │   ├── produce/            # 生产工单、报工记录、工资列表
│   │   │   ├── stock/              # 库存货架
│   │   │   ├── finance/            # 财务（预留）
│   │   │   ├── report/             # 报表（预留）
│   │   │   └── common/             # 通用布局组件
│   │   ├── router/                 # 路由（动态路由 + 权限守卫）
│   │   ├── store/                  # Pinia 状态（登录用户/权限）
│   │   ├── components/             # 通用组件
│   │   ├── directive/              # 自定义指令（按钮权限 v-perm）
│   │   ├── utils/                  # 工具：axios 封装、打印模板渲染器 cuttingSheetRenderer
│   │   └── types/                  # TS 类型定义
│   ├── vite.config.ts              # Vite 配置（开发端口 5173）
│   └── package.json
│
├── guxian-wx-miniprogram/          # 微信小程序：工人端（扫码报工）
│   ├── app.js / app.json / app.wxss
│   ├── pages/
│   │   ├── login/                  # 工人微信登录
│   │   ├── task/                   # 我的在制工单
│   │   ├── report/                 # 扫生产单二维码 → 选工序 → 报工计件
│   │   └── wage/                   # 我的计件工资
│   └── utils/request.js            # 小程序请求封装（带 token）
│
├── 产品公式明细/                    # 各产品下料/剪网尺寸公式图片资料（业务配置参考）
├── dev-tools/                      # 开发调试脚本与日志（不入库发布）
└── .gitignore
```

---

## 二、核心业务流程

```text
客户建档 → 销售下单（选产品+填尺寸）→ 按产品公式自动算下料/剪网尺寸
       → 订单审核 → 自动生成生产工单 → 打印生产单（含扫码二维码）
       → 工人微信扫码 → 选工序报工（开料/剪网/组装/打包）→ 计件工资自动累加
       → 完工入库上架 → 交付客户
```

## 三、关键约定

| 项 | 说明 |
|---|---|
| 订单号规则 | 年月日 + X + 当日序号（如 `20261011X001`） |
| 工单号 | 与订单编号一致 |
| 产品主数据 | 存于系统管理 → 字典管理（`sys_dict_type` / `sys_dict_data`），产品公式存 `formula_config` |
| 打印模板 | 存于 `t_print_template`，拖拽式设计器，工单打印自动套用默认模板 |
| 二维码 | 生产单二维码内容 `GX-WORK:{workId}:{工单号}`，小程序扫码直达报工页 |
| 服务端口 | 网关 8080 / system 8081 / customer 8082 / sales 8083 / produce 8085 / mini-api 8088 / 前端 5173 |

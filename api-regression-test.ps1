# ============================================================
# 古贤 ERP 全项目接口回归测试脚本 v3
# 覆盖：鉴权 / 系统管理(用户·角色·菜单·字典·打印模板·日志) / 客户·跟进 / 销售订单·产品BOM
# 说明：Windows PowerShell 5.1
#   - 脚本需 UTF-8 BOM
#   - 请求体按 UTF-8 字节发送
#   - 响应统一用 UTF-8 解码（PS 5.1 的 Invoke-RestMethod 会把 UTF-8 JSON 按 Latin-1 解，导致中文乱码）
# ============================================================
$ErrorActionPreference = 'Continue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$SYS = 'http://127.0.0.1:8081'
$CUS = 'http://127.0.0.1:8082'
$SAL = 'http://127.0.0.1:8083'

$script:pass = 0
$script:fail = 0
$script:fails = @()

function Api([string]$method, [string]$url, $body = $null, [hashtable]$headers = $null) {
  $req = [System.Net.WebRequest]::Create($url)
  $req.Method = $method
  $req.Timeout = 25000
  if ($headers) { foreach ($k in $headers.Keys) { $req.Headers.Add($k, [string]$headers[$k]) } }
  if ($null -ne $body) {
    $json = $body | ConvertTo-Json -Depth 8 -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $req.ContentType = 'application/json; charset=utf-8'
    $req.ContentLength = $bytes.Length
    $s = $req.GetRequestStream()
    $s.Write($bytes, 0, $bytes.Length)
    $s.Close()
  }
  try { $resp = $req.GetResponse() } catch [System.Net.WebException] { $resp = $_.Exception.Response }
  if ($null -eq $resp) { return $null }
  $reader = New-Object System.IO.StreamReader($resp.GetResponseStream(), [System.Text.Encoding]::UTF8)
  $text = $reader.ReadToEnd()
  $reader.Close()
  $resp.Close()
  if ([string]::IsNullOrWhiteSpace($text)) { return $null }
  return $text | ConvertFrom-Json
}

function Login([string]$u, [string]$p) { return Api 'POST' "$SYS/system/login/doLogin" @{ username = $u; password = $p } }

$login = Login 'clerk' '123456'
if ($login.code -ne 200) { Write-Host "登录失败，终止测试: $($login.msg)" -ForegroundColor Red; return }
$H = @{ Authorization = "Bearer $($login.data.token)" }

function T([string]$name, [string]$method, [string]$url, $body = $null, [int]$expect = 200) {
  $r = Api $method $url $body $H
  if ($null -eq $r) {
    $script:fail++; $script:fails += "$name -> 无响应"
    Write-Host ("EX    {0,-46}" -f $name) -ForegroundColor Red
    return $null
  }
  if ($r.code -eq $expect) {
    $script:pass++
    Write-Host ("PASS  {0,-46} code={1}" -f $name, $r.code) -ForegroundColor Green
  } else {
    $script:fail++; $script:fails += "$name -> code=$($r.code) msg=$($r.msg)"
    Write-Host ("FAIL  {0,-46} code={1} msg={2}" -f $name, $r.code, $r.msg) -ForegroundColor Yellow
  }
  return $r
}
function Section([string]$t) { Write-Host "`n===== $t =====" -ForegroundColor Cyan }
function Ok([string]$m) { $script:pass++; Write-Host "PASS  $m" -ForegroundColor Green }
function No([string]$m) { $script:fail++; $script:fails += $m; Write-Host "FAIL  $m" -ForegroundColor Yellow }

# ---------------- A. 鉴权 ----------------
Section 'A 鉴权'
$r = Api 'GET' "$SAL/sales/order/page"
if ($r -and $r.code -eq 401) { Ok '无 token 访问受保护接口 -> 401' } else { No "无 token 未拦截 code=$($r.code)" }

# ---------------- B. 登录 ----------------
Section 'B 登录'
$r = Login '卢总' '124300'
if ($r.code -eq 200) { Ok '明文密码账号 卢总/124300 可登录' } else { No "明文账号登录失败: $($r.msg)" }
$r = Login '郭总' '123456'
if ($r.code -eq 200) { Ok 'BCrypt 账号 郭总/123456 可登录' } else { No "BCrypt 账号登录失败: $($r.msg)" }

# ---------------- C. 用户 ----------------
Section 'C 用户管理'
T '用户分页' 'GET' "$SYS/system/user/page?pageNum=1&pageSize=5"
$u = T '新增用户' 'POST' "$SYS/system/user/save" @{ username = 'dsh_test_user'; realName = '测试用户DSH'; phone = '13900000000'; postType = 'clerk'; status = 1; roleIdList = @(2, 1) }
$uid = $null
if ($u) {
  $pg = Api 'GET' "$SYS/system/user/page?pageNum=1&pageSize=300" $null $H
  $row = $pg.data.records | Where-Object { $_.username -eq 'dsh_test_user' } | Select-Object -First 1
  if ($row) { $uid = $row.id }
}
if ($uid) {
  Write-Host "      新用户 id=$uid"
  $ri = T '查询用户角色 getRoleIds' 'GET' "$SYS/system/user/getRoleIds/$uid"
  if ($ri -and $ri.data.Count -ge 1) { Ok "用户角色回显 $($ri.data.Count) 条" } else { No '用户角色回显为空' }
  T '重置密码 resetPwd' 'PUT' "$SYS/system/user/resetPwd/$uid"
  $nl = Login 'dsh_test_user' '123456'
  if ($nl.code -eq 200) { Ok '新建用户默认密码 123456 可登录' } else { No "新建用户默认密码无法登录: $($nl.msg)" }
  T '删除用户' 'DELETE' "$SYS/system/user/$uid"
} else { No '新增用户后未能查到 id' }

# ---------------- D. 角色 ----------------
Section 'D 角色管理'
T '角色分页' 'GET' "$SYS/system/role/page?pageNum=1&pageSize=5"
T '角色全量' 'GET' "$SYS/system/role/listAll"
T '角色菜单树' 'GET' "$SYS/system/role/menuTree"
$roleSave = T '新增角色(带菜单)' 'POST' "$SYS/system/role/save" @{ roleName = '测试角色DSH'; roleCode = 'dsh_test_role'; sort = 99; menuIdList = @(40, 403) }
$rid = $null
if ($roleSave) {
  $ra = Api 'GET' "$SYS/system/role/listAll" $null $H
  $row = $ra.data | Where-Object { $_.roleCode -eq 'dsh_test_role' } | Select-Object -First 1
  if ($row) { $rid = $row.id }
}
if ($rid) {
  Write-Host "      新角色 id=$rid"
  $m = T '查询角色菜单 getMenuIds' 'GET' "$SYS/system/role/getMenuIds/$rid"
  if ($m -and $m.data.Count -eq 2) { Ok '角色菜单授权回显 2 条' } elseif ($m) { No "角色菜单回显异常: $($m.data -join ',')" }
  T '编辑角色' 'POST' "$SYS/system/role/save" @{ id = $rid; roleName = '测试角色DSH改'; roleCode = 'dsh_test_role'; sort = 98; menuIdList = @(40, 403, 404) }
  $m2 = Api 'GET' "$SYS/system/role/getMenuIds/$rid" $null $H
  if ($m2 -and $m2.data.Count -eq 3) { Ok '角色菜单编辑后回显 3 条' } else { No "角色菜单编辑后回显异常: $($m2.data -join ',')" }
  T '删除角色' 'DELETE' "$SYS/system/role/$rid"
} else { No '新增角色后未能查到 id' }

# ---------------- E. 菜单 ----------------
Section 'E 菜单管理'
T '用户菜单' 'GET' "$SYS/system/menu/getUserMenus"
T '菜单树' 'GET' "$SYS/system/menu/tree"
T '菜单分页' 'GET' "$SYS/system/menu/page?pageNum=1&pageSize=5"
$menuSave = T '新增菜单' 'POST' "$SYS/system/menu/save" @{ parentId = 40; menuName = '测试菜单DSH'; path = '/sales/dshTest'; perms = 'dsh:test:view'; menuType = 2; sort = 99 }
$mid = $null
if ($menuSave) {
  $mp = Api 'GET' "$SYS/system/menu/page?pageNum=1&pageSize=300" $null $H
  $row = $mp.data.records | Where-Object { $_.menuName -eq '测试菜单DSH' } | Select-Object -First 1
  if ($row) { $mid = $row.id }
}
if ($mid) {
  Write-Host "      新菜单 id=$mid"
  T '菜单详情' 'GET' "$SYS/system/menu/$mid"
  T '菜单更新' 'PUT' "$SYS/system/menu/update" @{ id = $mid; parentId = 40; menuName = '测试菜单DSH改'; path = '/sales/dshTest'; perms = 'dsh:test:view'; menuType = 2; sort = 99 }
  T '删除菜单' 'DELETE' "$SYS/system/menu/$mid"
} else { No '新增菜单后未能查到 id' }

# ---------------- F. 字典 ----------------
Section 'F 字典管理'
T '字典类型分页' 'GET' "$SYS/system/dict/type/page?pageNum=1&pageSize=5"
T '字典数据分页(不带 dictType)' 'GET' "$SYS/system/dict/data/page?pageNum=1&pageSize=5"
T '字典数据分页(带 dictType)' 'GET' "$SYS/system/dict/data/page?pageNum=1&pageSize=5&dictType=style_8k_sanjie"
$dt = T '新增字典类型' 'POST' "$SYS/system/dict/type/save" @{ dictName = '测试字典DSH'; dictType = 'dsh_test_type'; status = 1 }
$dtid = $null
if ($dt) {
  $tp = Api 'GET' "$SYS/system/dict/type/page?pageNum=1&pageSize=300" $null $H
  $row = $tp.data.records | Where-Object { $_.dictType -eq 'dsh_test_type' } | Select-Object -First 1
  if ($row) { $dtid = $row.id }
}
if ($dtid) {
  Write-Host "      新字典类型 id=$dtid"
  $dd = T '新增字典数据' 'POST' "$SYS/system/dict/data/save" @{ dictType = 'dsh_test_type'; dictLabel = '测试标签'; dictValue = '1'; sort = 1; status = 1 }
  $ddid = $null
  if ($dd) {
    $dp = Api 'GET' "$SYS/system/dict/data/page?pageNum=1&pageSize=300&dictType=dsh_test_type" $null $H
    $row = $dp.data.records | Select-Object -First 1
    if ($row) { $ddid = $row.id }
  }
  if ($ddid) {
    Write-Host "      新字典数据 id=$ddid"
    T '字典数据详情' 'GET' "$SYS/system/dict/data/$ddid"
    T '字典数据编辑' 'POST' "$SYS/system/dict/data/save" @{ id = $ddid; dictType = 'dsh_test_type'; dictLabel = '测试标签改'; dictValue = '1'; sort = 2; status = 1 }
    T '删除字典数据' 'DELETE' "$SYS/system/dict/data/$ddid"
  } else { No '新增字典数据后未查到 id' }
  T '字典类型详情' 'GET' "$SYS/system/dict/type/$dtid"
  T '删除字典类型' 'DELETE' "$SYS/system/dict/type/$dtid"
} else { No '新增字典类型后未查到 id' }

# ---------------- G. 打印模板 ----------------
Section 'G 打印模板'
T '模板类型下拉' 'GET' "$SYS/system/printTemplate/types"
T '模板分页' 'GET' "$SYS/system/printTemplate/page?pageNum=1&pageSize=5"
$pt = T '新增模板' 'POST' "$SYS/system/printTemplate/save" @{ templateName = '测试模板DSH'; templateType = 'sales_order'; paperSize = 'A4'; templateContent = '<div>测试内容</div>'; isDefault = 0; status = 1 }
$ptid = $null
if ($pt) {
  $pp = Api 'GET' "$SYS/system/printTemplate/page?pageNum=1&pageSize=300" $null $H
  $row = $pp.data.records | Where-Object { $_.templateName -eq '测试模板DSH' } | Select-Object -First 1
  if ($row) { $ptid = $row.templateId }
}
if ($ptid) {
  Write-Host "      新模板 id=$ptid"
  T '模板回显 getById' 'GET' "$SYS/system/printTemplate/getById/$ptid"
  T '模板预览 preview' 'GET' "$SYS/system/printTemplate/preview/$ptid"
  T '模板禁用 toggleStatus' 'PUT' "$SYS/system/printTemplate/toggleStatus/$ptid`?status=0"
  T '模板启用 toggleStatus' 'PUT' "$SYS/system/printTemplate/toggleStatus/$ptid`?status=1"
  T '删除模板' 'DELETE' "$SYS/system/printTemplate/delete/$ptid"
} else { No '新增模板后未查到 id' }

# ---------------- H. 操作日志 ----------------
Section 'H 操作日志'
T '操作日志分页' 'GET' "$SYS/system/operLog/page?pageNum=1&pageSize=5"

# ---------------- I. 客户与跟进 ----------------
Section 'I 客户与跟进'
T '客户分页' 'GET' "$CUS/customer/list/page?pageNum=1&pageSize=5"
T '客户全量' 'GET' "$CUS/customer/list/listAll"
$c1 = T '新增客户A' 'POST' "$CUS/customer/list/save" @{ customerName = '测试客户DSH-A'; contact = '张三'; phone = '13700000000'; address = '长沙市雨花区'; remark = '自动化测试' }
$c2 = T '新增客户B' 'POST' "$CUS/customer/list/save" @{ customerName = '测试客户DSH-B'; contact = '王五'; phone = '13700000002'; address = '长沙市天心区'; remark = '自动化测试' }
$cidA = $null; $cidB = $null
if ($c1 -and $c2) {
  $cp = Api 'GET' "$CUS/customer/list/page?pageNum=1&pageSize=300" $null $H
  $rowA = $cp.data.records | Where-Object { $_.customerName -eq '测试客户DSH-A' } | Select-Object -First 1
  $rowB = $cp.data.records | Where-Object { $_.customerName -eq '测试客户DSH-B' } | Select-Object -First 1
  if ($rowA) { $cidA = $rowA.customerId }
  if ($rowB) { $cidB = $rowB.customerId }
}
if ($cidA) {
  Write-Host "      新客户 id=$cidA / $cidB"
  T '客户详情' 'GET' "$CUS/customer/list/$cidA"
  T '客户编辑' 'POST' "$CUS/customer/list/save" @{ customerId = $cidA; customerName = '测试客户DSH-A改'; contact = '李四'; phone = '13700000001'; address = '长沙市岳麓区'; remark = '已修改' }
  T '新增跟进记录' 'POST' "$CUS/customer/follow/save" @{ customerId = $cidA; followContent = '首次电话沟通'; followTime = '2026-09-19T10:00:00'; followUser = '文员小张' }
  $fp = Api 'GET' "$CUS/customer/follow/page?pageNum=1&pageSize=50&customerId=$cidA" $null $H
  if ($fp.code -eq 200 -and $fp.data.total -ge 1) {
    Ok "按客户查跟进记录 total=$($fp.data.total)"
    T '删除跟进记录' 'DELETE' "$CUS/customer/follow/delete/$($fp.data.records[0].id)"
  } else { No '按客户查跟进记录为空' }
} else { No '新增客户后未能查到 id' }
$fa = Api 'GET' "$CUS/customer/follow/page?pageNum=1&pageSize=5" $null $H
if ($fa.code -eq 200 -and $fa.data.total -ge 1) { Ok "跟进分页(不带 customerId) 返回 $($fa.data.total) 条" } else { No "跟进分页(不带 customerId) 返回 $($fa.data.total) 条，应为全部" }
if ($cidA -and $cidB) { T '批量删除客户' 'DELETE' "$CUS/customer/list/batch" @($cidA, $cidB) }

# ---------------- J. 产品与BOM ----------------
Section 'J 产品与BOM'
T '产品分页' 'GET' "$SAL/sales/product/page?pageNum=1&pageSize=5"
T '产品启用列表' 'GET' "$SAL/sales/product/listEnabled"
T '物料列表' 'GET' "$SAL/sales/product/materials"
$pr = T '新增产品' 'POST' "$SAL/sales/product/save" @{ productCode = 'DSH-TEST-001'; productName = '测试产品DSH'; productType = '平开纱窗'; spec = '1000x1000'; unit = '㎡'; unitPrice = 150.00; priceType = 1; minArea = 0.5; defaultColor = '白色'; defaultMaterial = '铝合金'; openDirection = '外开'; status = 1; remark = '自动化测试' }
$prodId = $null
if ($pr) {
  $pp = Api 'GET' "$SAL/sales/product/page?pageNum=1&pageSize=300" $null $H
  $row = $pp.data.records | Where-Object { $_.productCode -eq 'DSH-TEST-001' } | Select-Object -First 1
  if ($row) { $prodId = $row.productId }
}
if ($prodId) {
  Write-Host "      新产品 id=$prodId"
  T '产品详情' 'GET' "$SAL/sales/product/$prodId"
  T '产品BOM查询' 'GET' "$SAL/sales/product/bom/$prodId"
  $mats = Api 'GET' "$SAL/sales/product/materials" $null $H
  if ($mats.code -eq 200 -and $mats.data.Count -ge 2) {
    $bomRows = @(
      @{ materialId = $mats.data[0].id; useNum = 2.5; lossRate = 0.05; sort = 1 },
      @{ materialId = $mats.data[1].id; useNum = 1.0; lossRate = 0.03; sort = 2 }
    )
    T '产品BOM保存' 'POST' "$SAL/sales/product/bom/save/$prodId" $bomRows
    $bom = Api 'GET' "$SAL/sales/product/bom/$prodId" $null $H
    if ($bom.code -eq 200 -and $bom.data.Count -ge 2) { Ok "BOM 保存后回读 $($bom.data.Count) 条" } else { No "BOM 保存后回读异常 count=$($bom.data.Count)" }
  } else { No '物料列表不足 2 条，跳过 BOM 测试' }
  T '删除产品' 'DELETE' "$SAL/sales/product/$prodId"
} else { No '新增产品后未查到 id' }

# ---------------- K. 销售订单 ----------------
Section 'K 销售订单'
T '订单分页' 'GET' "$SAL/sales/order/page?pageNum=1&pageSize=5"
T '明细统计' 'GET' "$SAL/sales/order/item/stats"
T '字典选项' 'GET' "$SAL/sales/order/dict/options"
$op = Api 'GET' "$SAL/sales/order/page?pageNum=1&pageSize=20" $null $H
$doneOrder = $op.data.records | Where-Object { $_.orderStatus -eq 5 } | Select-Object -First 1
if ($doneOrder) {
  $oid = $doneOrder.orderId
  Write-Host "      已完成订单 id=$oid No=$($doneOrder.orderNo)"
  $r = T '已完成订单再受理(应业务失败)' 'PUT' "$SAL/sales/order/audit/pass/$oid" $null 500
  if ($r -and $r.msg -like '*未受理*') { Ok "非法流转返回业务提示：$($r.msg)" } elseif ($r) { No "非法流转提示不友好: $($r.msg)" }
  $r = T '已完成订单删除(应业务失败)' 'DELETE' "$SAL/sales/order/$oid" $null 500
  if ($r -and $r.msg -like '*不允许删除*') { Ok "删除保护生效：$($r.msg)" }
  T '收款金额为0(应业务失败)' 'PUT' "$SAL/sales/order/receive/$oid" @{ amount = 0 } 500
}
T '不存在的订单详情(应业务失败)' 'GET' "$SAL/sales/order/99999999" $null 500

# ---------------- 汇总 ----------------
Write-Host "`n================ 汇总 ================" -ForegroundColor Cyan
Write-Host ("通过 {0} 项，失败 {1} 项" -f $script:pass, $script:fail)
if ($script:fails.Count -gt 0) {
  Write-Host "`n失败清单：" -ForegroundColor Yellow
  $script:fails | ForEach-Object { Write-Host "  - $_" }
}

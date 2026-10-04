$ErrorActionPreference = "SilentlyContinue"
$login = curl.exe -s -X POST "http://127.0.0.1:8081/system/login/doLogin" -H "Content-Type: application/json" -d '{"username":"clerk","password":"123456"}' | ConvertFrom-Json
$token = $login.data.token
if (-not $token) { Write-Host "LOGIN FAILED: $($login.msg)"; exit }
Write-Host "login OK"
$H = @{ "Authorization" = "Bearer $token" }
$tests = @(
  @{ m="GET";  u="http://127.0.0.1:8081/system/dict/type/page"; q="?pageNum=1&pageSize=10" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/dict/data/page"; q="?pageNum=1&pageSize=10" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/menu/tree" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/menu/getUserMenus" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/user/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/role/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/role/listAll" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/operLog/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/printTemplate/page"; q="?pageNum=1&pageSize=20" },
  @{ m="GET";  u="http://127.0.0.1:8081/system/printTemplate/types" },
  @{ m="GET";  u="http://127.0.0.1:8082/customer/list/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8082/customer/list/listAll" },
  @{ m="GET";  u="http://127.0.0.1:8082/customer/follow/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8083/sales/order/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8083/sales/order/dict/options" },
  @{ m="GET";  u="http://127.0.0.1:8083/sales/order/item/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8083/sales/product/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8083/sales/product/listEnabled" },
  @{ m="GET";  u="http://127.0.0.1:8084/stock/shelf/list" },
  @{ m="GET";  u="http://127.0.0.1:8084/stock/shelf/listEnabled" },
  @{ m="GET";  u="http://127.0.0.1:8085/production/workorder/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8085/production/process/list" },
  @{ m="GET";  u="http://127.0.0.1:8085/production/process/listEnabled" },
  @{ m="GET";  u="http://127.0.0.1:8085/production/common/users" },
  @{ m="GET";  u="http://127.0.0.1:8085/production/report/page"; q="?pageNum=1&pageSize=5" },
  @{ m="GET";  u="http://127.0.0.1:8085/production/report/wage/summary" },
  @{ m="GET";  u="http://127.0.0.1:8085/production/workorder/auditedOrders" },
  @{ m="GET";  u="http://127.0.0.1:8080/system/dict/type/page"; q="?pageNum=1&pageSize=5" }
)
foreach ($t in $tests) {
  if ($t.q) { $url = $t.u + $t.q } else { $url = $t.u }
  $code = curl.exe -s -o "D:\Java\guxian\resp_tmp.json" -w "%{http_code}" -H "Authorization: Bearer $token" "$url"
  $body = ""
  if (Test-Path "D:\Java\guxian\resp_tmp.json") { $body = (Get-Content "D:\Java\guxian\resp_tmp.json" -Raw) }
  $code200 = ""
  if ($body -match '"code":(\d+)') { $code200 = "resp:" + $Matches[1] }
  $msg = ""
  if ($body -match '"msg":"([^"]{0,40})') { $msg = $Matches[1] }
  $short = $url -replace "http://127.0.0.1:808\d",""
  Write-Host ("{0,-4} {1,-42} http={2,-3} {3} {4}" -f $t.m, $short, $code, $code200, $msg)
}


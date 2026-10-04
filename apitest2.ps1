$ErrorActionPreference = "SilentlyContinue"
$login = curl.exe -s -X POST "http://127.0.0.1:8081/system/login/doLogin" -H "Content-Type: application/json" --data-binary "@D:\Java\guxian\lg.json" | ConvertFrom-Json
$token = $login.data.token
if (-not $token) { Write-Host "LOGIN FAILED"; exit }
$tests = @(
  @{ u="http://127.0.0.1:8081/system/dict/type/page?pageNum=1&pageSize=10" },
  @{ u="http://127.0.0.1:8081/system/dict/data/page?pageNum=1&pageSize=10" },
  @{ u="http://127.0.0.1:8081/system/menu/tree" },
  @{ u="http://127.0.0.1:8081/system/menu/getUserMenus" },
  @{ u="http://127.0.0.1:8081/system/user/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8081/system/role/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8081/system/role/listAll" },
  @{ u="http://127.0.0.1:8081/system/operLog/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8081/system/printTemplate/page?pageNum=1&pageSize=20" },
  @{ u="http://127.0.0.1:8081/system/printTemplate/types" },
  @{ u="http://127.0.0.1:8082/customer/list/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8082/customer/list/listAll" },
  @{ u="http://127.0.0.1:8082/customer/follow/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8083/sales/order/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8083/sales/order/dict/options" },
  @{ u="http://127.0.0.1:8083/sales/order/item/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8083/sales/product/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8083/sales/product/listEnabled" },
  @{ u="http://127.0.0.1:8084/stock/shelf/list" },
  @{ u="http://127.0.0.1:8084/stock/shelf/listEnabled" },
  @{ u="http://127.0.0.1:8085/production/workorder/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8085/production/process/list" },
  @{ u="http://127.0.0.1:8085/production/process/listEnabled" },
  @{ u="http://127.0.0.1:8085/production/common/users" },
  @{ u="http://127.0.0.1:8085/production/report/page?pageNum=1&pageSize=5" },
  @{ u="http://127.0.0.1:8085/production/report/wage/summary" },
  @{ u="http://127.0.0.1:8085/production/workorder/auditedOrders" },
  @{ u="http://127.0.0.1:8080/system/dict/type/page?pageNum=1&pageSize=5" }
)
foreach ($t in $tests) {
  $code = curl.exe -s -o "D:\Java\guxian\resp_tmp.json" -w "%{http_code}" -H "Authorization: Bearer $token" $t.u
  $body = Get-Content "D:\Java\guxian\resp_tmp.json" -Raw
  $resp = ""; $msg = ""; $total=""
  if ($body -match '"code":(\d+)') { $resp = "resp:" + $Matches[1] }
  if ($body -match '"msg":"([^"]{0,30})') { $msg = $Matches[1] }
  if ($body -match '"total":(\d+)') { $total = " total=" + $Matches[1] }
  $short = $t.u -replace "http://127.0.0.1:808\d",""
  Write-Host ("{0,-50} http={1,-3} {2} {3} {4}" -f $short, $code, $resp, $msg, $total)
}

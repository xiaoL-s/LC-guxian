$roots = @("D:\Java\guxian\guxian-erp-parent\guxian-system\src\main\java","D:\Java\guxian\guxian-erp-parent\guxian-customer\src\main\java","D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java","D:\Java\guxian\guxian-erp-parent\guxian-stock\src\main\java","D:\Java\guxian\guxian-erp-parent\guxian-produce\src\main\java")
$fe = @("")
$be = @{}
foreach($r in $roots){
  Get-ChildItem $r -Recurse -Filter *Controller.java | ForEach-Object {
    $c = Get-Content $_.FullName -Raw
    $cls = $null
    if ($c -match 'class\s+(\w+)Controller') { $cls = $_.FullName -replace ".*\\","" }
    if ($c -match '@RequestMapping\("([^"]+)"\)') { $base = $Matches[1] } else { $base = "" }
    [regex]::Matches($c, '@(Get|Post|Put|Delete)Mapping\("([^"]+)"\)') | ForEach-Object {
      $full = $base + $_.Groups[2].Value
      if ($full -match '\{(\w+)\}') {
        # 模板参数转通配
        $full = $full -replace '\{(\w+)\}', ':$1'
      }
      $be[$full] = $true
    }
    [regex]::Matches($c, '@(Get|Post|Put|Delete)Mapping\("([^"]+)"\)') | ForEach-Object {}
  }
}
$fe = Get-Content "D:\Java\guxian\fe_api.ps1" -Raw
# 简化：直接读前端清单文件
$list = @("/customer/follow/page","/customer/follow/save","/customer/list/batch","/customer/list/listAll","/customer/list/page","/customer/list/save","/production/common/instock","/production/common/users","/production/process/list","/production/process/listEnabled","/production/process/save","/production/report/page","/production/report/wage/byWorker","/production/report/wage/summary","/production/workorder/auditedOrders","/production/workorder/finish","/production/workorder/page","/production/workorder/report","/production/workorder/split","/sales/order/dict/options","/sales/order/item/page","/sales/order/item/stats","/sales/order/page","/sales/order/save","/sales/product/listEnabled","/sales/product/page","/sales/product/save","/stock/shelf/list","/stock/shelf/listEnabled","/stock/shelf/save","/system/dict/data/formula/test","/system/dict/data/page","/system/dict/data/save","/system/dict/type/page","/system/dict/type/save","/system/login/doLogin","/system/menu/getUserMenus","/system/menu/page","/system/menu/save","/system/menu/tree","/system/operLog/page","/system/printTemplate/page","/system/printTemplate/save","/system/printTemplate/types","/system/role/listAll","/system/role/menuTree","/system/role/page","/system/role/save","/system/user/page","/system/user/save")
Write-Host "=== 后端接口 ==="
$be.Keys | Sort-Object | ForEach-Object { Write-Host $_ }

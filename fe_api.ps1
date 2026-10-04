$roots = @("D:\Java\guxian\guxian-erp-parent\guxian-system\src\main\java","D:\Java\guxian\guxian-erp-parent\guxian-customer\src\main\java","D:\Java\guxian\guxian-erp-parent\guxian-sales\src\main\java","D:\Java\guxian\guxian-erp-parent\guxian-stock\src\main\java","D:\Java\guxian\guxian-erp-parent\guxian-produce\src\main\java")
$api = @{}
Get-ChildItem "D:\Java\guxian\guxian-erp-screen\src\api" -Recurse -Filter *.ts | ForEach-Object {
  $c = Get-Content $_.FullName -Raw
  [regex]::Matches($c, "url:\s*'([^']+)'") | ForEach-Object { $api[$_.Groups[1].Value] = $true }
  [regex]::Matches($c, "request\.(get|post|put|delete)\(`?['""]([^'""`]+)") | ForEach-Object { $api[$_.Groups[2].Value] = $true }
}
$fe = $api.Keys | Where-Object { $_ -match "^/" } | Sort-Object -Unique
Write-Host "=== 前端API总数: $($fe.Count) ==="
foreach($u in $fe){ Write-Host $u }

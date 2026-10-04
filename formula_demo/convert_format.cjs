// 兼容性验证：模拟 DictFormulaManager 的 parse+build 逻辑（与组件代码一致）
// 将库中旧结构 formula_config（vars 为字符串数组）转为新结构（vars 为对象数组、分组为行数组）
const { execFileSync } = require('child_process');
const MYSQL = 'C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe';
const ARGS = ['--host=127.0.0.1', '--user=root', '--password=root', '--default-character-set=utf8mb4', '--database=guxian-erp', '--batch', '--raw', '--skip-column-names'];
function q(sql) { return execFileSync(MYSQL, ARGS.concat(['--execute=' + sql]), { encoding: 'utf8' }).trim(); }

const FIXED = ['vars', '型材', '纱网', '面积', '金额明细'];
const emptyRow = () => ({ cond: '', expr: '', mult: '', w: '', h: '' });
function toRows(node) { if (!node) return []; const list = Array.isArray(node) ? node : [node]; return list.map(r => ({ cond: r.cond || '', expr: r.expr || '', mult: r.mult || '', w: r.w || '', h: r.h || '' })); }
function parse(json) {
  const model = { vars: [], varGroups: [], profiles: [], nets: [], areas: [], amounts: [] };
  if (!json) return model;
  const cfg = JSON.parse(json);
  const vs = Array.isArray(cfg.vars) ? cfg.vars : [];
  model.vars = vs.map(v => typeof v === 'string' ? { name: v, list: '', def: '', sort: 0 } : { name: v.name || '', list: v.list || '', def: v.default !== undefined ? String(v.default) : '', sort: v.sort || 0 });
  Object.keys(cfg).forEach(k => { if (FIXED.includes(k)) return; model.varGroups.push({ key: k, rows: toRows(cfg[k]) }); });
  Object.entries(cfg['型材'] || {}).forEach(([name, node]) => model.profiles.push({ name, rows: toRows(node) }));
  Object.entries(cfg['纱网'] || {}).forEach(([name, node]) => model.nets.push({ name, rows: toRows(node) }));
  Object.entries(cfg['金额明细'] || {}).forEach(([name, node]) => model.amounts.push({ name, rows: toRows(node) }));
  model.areas = toRows(cfg['面积']);
  return model;
}
function rowToObj(r) { const o = {}; if (r.cond.trim()) o.cond = r.cond.trim(); if (r.expr.trim()) o.expr = r.expr.trim(); if (r.mult.trim()) o.mult = r.mult.trim(); if (r.w.trim()) o.w = r.w.trim(); if (r.h.trim()) o.h = r.h.trim(); return o; }
function build(m) {
  const cfg = {};
  cfg.vars = m.vars.filter(v => v.name.trim()).map(v => { const o = { name: v.name.trim() }; if (v.list.trim()) o.list = v.list.trim(); if (v.def !== '') { const num = Number(v.def); o.default = (v.def.trim() !== '' && !isNaN(num)) ? num : v.def; } if (Number(v.sort)) o.sort = Number(v.sort); return o; });
  m.varGroups.forEach(g => { const rows = g.rows.filter(r => r.expr.trim()).map(rowToObj); if (rows.length) cfg[g.key] = rows; });
  const pf = {}; m.profiles.forEach(p => { const rows = p.rows.filter(r => r.expr.trim()).map(rowToObj); if (p.name.trim() && rows.length) pf[p.name.trim()] = rows; }); if (Object.keys(pf).length) cfg['型材'] = pf;
  const nt = {}; m.nets.forEach(p => { const rows = p.rows.filter(r => r.w.trim() || r.h.trim()).map(rowToObj); if (p.name.trim() && rows.length) nt[p.name.trim()] = rows; }); if (Object.keys(nt).length) cfg['纱网'] = nt;
  const ar = m.areas.filter(r => r.expr.trim()).map(rowToObj); if (ar.length) cfg['面积'] = ar;
  const am = {}; m.amounts.forEach(p => { const rows = p.rows.filter(r => r.expr.trim()).map(rowToObj); if (p.name.trim() && rows.length) am[p.name.trim()] = rows; }); if (Object.keys(am).length) cfg['金额明细'] = am;
  return JSON.stringify(cfg);
}

for (const id of [73, 20]) {
  const old = q(`SELECT formula_config FROM sys_dict_data WHERE id=${id}`);
  const model = parse(old);
  const next = build(model);
  const esc = next.replace(/'/g, "''");
  q(`UPDATE sys_dict_data SET formula_config='${esc}' WHERE id=${id}`);
  console.log(`id=${id}: len ${old.length} -> ${next.length}, groups=${model.varGroups.map(g => g.key).join(',')}`);
}
console.log('CONVERT_DONE');

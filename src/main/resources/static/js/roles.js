// 角色權限管理頁，分成三個區塊：
// 1. 用戶角色總覽：為每個用戶勾選角色
// 2. 角色管理：角色的新增 / 改名 / 刪除
// 3. 權限規則管理：URL pattern 與可存取角色的對應（DynamicAuthorizationFilter 依此判斷權限）

const RULE_API = '/api/roles/mappings';
const ROLE_API = '/api/roles';
const USER_API = '/api/users';

// 目前所有角色；用戶角色總覽需要用它產生勾選框，所以角色有變動時要重新載入兩個區塊
let allRoles = [];

// 產生「輸入框 + 更新 / 刪除按鈕」的清單項目，角色與規則清單共用。
// 按鈕帶 data-action，點擊由外層 <ul> 的事件委派統一處理。
// 外觀用 Bootstrap 的 list-group-item 與 input-group（輸入框和按鈕黏成一排）。
function createEditableItem(id, inputs) {
  const li = document.createElement('li');
  li.className = 'list-group-item';
  li.dataset.id = id;

  const group = document.createElement('div');
  group.className = 'input-group input-group-sm';
  inputs.forEach(({ name, value, className = '' }) => {
    const input = document.createElement('input');
    input.name = name;
    input.value = value ?? '';
    input.className = `form-control ${className}`;
    input.setAttribute('aria-label', name);
    group.append(input);
  });
  [
    ['update', '更新', 'btn-outline-primary'],
    ['delete', '刪除', 'btn-outline-danger'],
  ].forEach(([action, text, style]) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `btn ${style}`;
    btn.dataset.action = action;
    btn.textContent = text;
    group.append(btn);
  });

  li.append(group);
  return li;
}

// 讀取清單項目中某個輸入框的值
function itemValue(li, name) {
  return li.querySelector(`input[name="${name}"]`).value.trim();
}

// 事件委派（event delegation）：清單每次重新載入都會換掉裡面的按鈕，
// 所以監聽器掛在不會被換掉的 <ul> 上，再依 data-action 決定要做什麼。
function onListAction(listId, handlers) {
  document.getElementById(listId).addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action]');
    if (btn) handlers[btn.dataset.action](btn.closest('li'));
  });
}

// ================================
// 用戶角色總覽
// ================================

async function loadUserRoles() {
  const container = document.getElementById('user-roles-container');
  try {
    const users = await apiFetch(USER_API);
    container.innerHTML = '';
    users.forEach((user) => container.appendChild(createUserCard(user)));
  } catch (err) {
    alert(`無法載入用戶清單：${err.message}`);
  }
}

// 外層 col 讓卡片在格線（row）裡排成多欄：md 兩欄、xl 三欄
function createUserCard(user) {
  const col = document.createElement('div');
  col.className = 'col-md-6 col-xl-4';

  const card = document.createElement('div');
  card.className = 'card h-100';
  const body = document.createElement('div');
  body.className = 'card-body d-flex flex-column';

  const header = document.createElement('h3');
  header.className = 'h6 card-title mb-1';
  header.textContent = `👤 ${user.username}`;

  const currentRoles = document.createElement('p');
  currentRoles.className = 'small text-secondary mb-2';
  currentRoles.textContent = `現有角色：${user.roles?.slice().sort().join(', ') || '無角色'}`;

  const checkboxArea = document.createElement('div');
  checkboxArea.className = 'mb-3';
  renderRoleCheckboxes(checkboxArea, allRoles, user.roles || []);

  const saveBtn = document.createElement('button');
  saveBtn.type = 'button';
  // mt-auto：把按鈕推到卡片底部，各卡片高度不同時按鈕仍對齊
  saveBtn.className = 'btn btn-sm btn-primary mt-auto align-self-start';
  saveBtn.textContent = '保存角色變更';
  saveBtn.addEventListener('click', () => saveUserRoles(user, checkboxArea));

  body.append(header, currentRoles, checkboxArea, saveBtn);
  card.append(body);
  col.append(card);
  return col;
}

async function saveUserRoles(user, checkboxArea) {
  // UserUpdateRequest 是部分更新：只送 roleIds，其他欄位為 null 不會被修改
  const roleIds = getCheckedRoleIds(checkboxArea);
  try {
    await apiFetch(`${USER_API}/${user.id}`, { method: 'PUT', body: { roleIds } });
    alert(`用戶 ${user.username} 角色更新成功`);
    loadUserRoles();
  } catch (err) {
    alert(`更新失敗：${err.message}`);
  }
}

// ================================
// 角色管理
// ================================

async function loadRoles() {
  try {
    allRoles = await apiFetch(ROLE_API);
  } catch (err) {
    alert(`載入角色失敗：${err.message}`);
    return;
  }
  const list = document.getElementById('role-list');
  list.innerHTML = '';
  allRoles.forEach((role) => {
    list.appendChild(createEditableItem(role.id, [{ name: 'name', value: role.name }]));
  });
}

// 角色變動後，角色清單與用戶角色總覽都要更新（總覽要等 allRoles 更新完才能畫）
async function reloadRolesAndUsers() {
  await loadRoles();
  await loadUserRoles();
}

async function createRole() {
  const name = document.getElementById('role-name').value.trim();
  if (!name) return alert('請輸入角色名稱！');

  try {
    await apiFetch(ROLE_API, { method: 'POST', body: { name } });
    alert('新增角色成功');
    document.getElementById('role-form').reset();
    reloadRolesAndUsers();
  } catch (err) {
    alert(`新增角色失敗：${err.message}`);
  }
}

async function updateRole(li) {
  const name = itemValue(li, 'name');
  if (!name) return alert('請輸入角色名稱！');

  try {
    await apiFetch(`${ROLE_API}/${li.dataset.id}`, { method: 'PUT', body: { name } });
    alert('更新角色成功');
    reloadRolesAndUsers();
  } catch (err) {
    alert(`更新角色失敗：${err.message}`);
  }
}

async function deleteRole(li) {
  if (!confirm(`確定要刪除角色 ${itemValue(li, 'name')} 嗎?`)) return;

  try {
    await apiFetch(`${ROLE_API}/${li.dataset.id}`, { method: 'DELETE' });
    alert('刪除角色成功');
    reloadRolesAndUsers();
  } catch (err) {
    alert(`刪除角色失敗：${err.message}`);
  }
}

// ================================
// 權限規則管理
// ================================

async function loadRules() {
  let rules;
  try {
    rules = await apiFetch(RULE_API);
  } catch (err) {
    alert(`載入規則失敗：${err.message}`);
    return;
  }
  const list = document.getElementById('rule-list');
  list.innerHTML = '';
  rules.forEach((rule) => {
    list.appendChild(
      createEditableItem(rule.id, [
        { name: 'urlPattern', value: rule.urlPattern, className: 'font-monospace-input' },
        { name: 'roles', value: rule.roles },
      ]),
    );
  });
}

async function createRule() {
  const urlPattern = document.getElementById('rule-url-pattern').value.trim();
  const roles = document.getElementById('rule-roles').value.trim();
  if (!urlPattern || !roles) return alert('請完整輸入！');

  try {
    await apiFetch(RULE_API, { method: 'POST', body: { urlPattern, roles } });
    alert('新增成功');
    document.getElementById('rule-form').reset();
    loadRules();
  } catch (err) {
    alert(`新增失敗：${err.message}`);
  }
}

async function updateRule(li) {
  const urlPattern = itemValue(li, 'urlPattern');
  const roles = itemValue(li, 'roles');
  if (!urlPattern || !roles) return alert('請完整輸入！');

  try {
    await apiFetch(`${RULE_API}/${li.dataset.id}`, {
      method: 'PUT',
      body: { urlPattern, roles },
    });
    alert('更新成功');
    loadRules();
  } catch (err) {
    alert(`更新失敗：${err.message}`);
  }
}

async function deleteRule(li) {
  if (!confirm(`確定要刪除規則 ${itemValue(li, 'urlPattern')} 嗎?`)) return;

  try {
    await apiFetch(`${RULE_API}/${li.dataset.id}`, { method: 'DELETE' });
    alert('刪除成功');
    loadRules();
  } catch (err) {
    alert(`刪除失敗：${err.message}`);
  }
}

// ================================
// 頁面載入：綁定事件並載入資料
// ================================

document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuthOrRedirect()) return;

  document.getElementById('role-form').addEventListener('submit', (e) => {
    e.preventDefault();
    createRole();
  });
  document.getElementById('rule-form').addEventListener('submit', (e) => {
    e.preventDefault();
    createRule();
  });
  onListAction('role-list', { update: updateRole, delete: deleteRole });
  onListAction('rule-list', { update: updateRule, delete: deleteRule });

  loadRules();
  reloadRolesAndUsers(); // 用戶總覽需要 allRoles，所以先載角色再載用戶
});

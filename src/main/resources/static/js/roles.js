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

// 檢查清單項目裡的輸入框都有填；空的欄位標紅並回傳 false
function requireItemValues(li, names) {
  let ok = true;
  names.forEach((name) => {
    const input = li.querySelector(`input[name="${name}"]`);
    input.classList.toggle('is-invalid', !input.value.trim());
    if (!input.value.trim()) ok = false;
  });
  if (!ok) showToast('請填寫所有欄位', 'warning');
  return ok;
}

// 事件委派（event delegation）：清單每次重新載入都會換掉裡面的按鈕，
// 所以監聽器掛在不會被換掉的 <ul> 上，再依 data-action 決定要做什麼。
// 把被點的按鈕也傳給 handler，讓它在呼叫 API 時用 withLoading 顯示載入中。
function onListAction(listId, handlers) {
  document.getElementById(listId).addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action]');
    if (btn) handlers[btn.dataset.action](btn.closest('li'), btn);
  });
}

// ================================
// 用戶角色總覽
// ================================

async function loadUserRoles() {
  const container = document.getElementById('user-roles-container');
  try {
    const users = await apiFetch(USER_API);
    container.replaceChildren(...users.map(createUserCard));
  } catch (err) {
    container.innerHTML = '<p class="text-danger mb-0">用戶清單載入失敗</p>';
    showToast(`無法載入用戶清單：${err.message}`, 'danger');
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
  saveBtn.addEventListener('click', () =>
    withLoading(saveBtn, () => saveUserRoles(user, checkboxArea)),
  );

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
    showToast(`已更新 ${user.username} 的角色`);
    loadUserRoles();
  } catch (err) {
    showToast(`更新失敗：${err.message}`, 'danger');
  }
}

// ================================
// 角色管理
// ================================

async function loadRoles() {
  const list = document.getElementById('role-list');
  try {
    allRoles = await apiFetch(ROLE_API);
  } catch (err) {
    list.innerHTML = '<li class="list-group-item text-danger">角色載入失敗</li>';
    showToast(`載入角色失敗：${err.message}`, 'danger');
    return;
  }
  list.replaceChildren(
    ...allRoles.map((role) => createEditableItem(role.id, [{ name: 'name', value: role.name }])),
  );
}

// 角色變動後，角色清單與用戶角色總覽都要更新（總覽要等 allRoles 更新完才能畫）
async function reloadRolesAndUsers() {
  await loadRoles();
  await loadUserRoles();
}

async function createRole() {
  const name = document.getElementById('role-name').value.trim();

  try {
    await apiFetch(ROLE_API, { method: 'POST', body: { name } });
    showToast(`已新增角色 ${name}`);
    document.getElementById('role-form').reset();
    reloadRolesAndUsers();
  } catch (err) {
    showToast(`新增角色失敗：${err.message}`, 'danger');
  }
}

async function updateRole(li, btn) {
  if (!requireItemValues(li, ['name'])) return;
  const name = itemValue(li, 'name');

  await withLoading(btn, async () => {
    try {
      await apiFetch(`${ROLE_API}/${li.dataset.id}`, { method: 'PUT', body: { name } });
      showToast(`角色已更新為 ${name}`);
      reloadRolesAndUsers();
    } catch (err) {
      showToast(`更新角色失敗：${err.message}`, 'danger');
    }
  });
}

async function deleteRole(li, btn) {
  const name = itemValue(li, 'name');
  const ok = await confirmDialog(`確定要刪除角色 ${name} 嗎？擁有此角色的用戶會失去它。`, {
    title: '刪除角色',
    confirmText: '刪除',
  });
  if (!ok) return;

  // 確認後才進入載入中狀態
  await withLoading(btn, async () => {
    try {
      await apiFetch(`${ROLE_API}/${li.dataset.id}`, { method: 'DELETE' });
      showToast(`已刪除角色 ${name}`);
      reloadRolesAndUsers();
    } catch (err) {
      showToast(`刪除角色失敗：${err.message}`, 'danger');
    }
  });
}

// ================================
// 權限規則管理
// ================================

async function loadRules() {
  const list = document.getElementById('rule-list');
  let rules;
  try {
    rules = await apiFetch(RULE_API);
  } catch (err) {
    list.innerHTML = '<li class="list-group-item text-danger">規則載入失敗</li>';
    showToast(`載入規則失敗：${err.message}`, 'danger');
    return;
  }
  list.replaceChildren(
    ...rules.map((rule) =>
      createEditableItem(rule.id, [
        { name: 'urlPattern', value: rule.urlPattern, className: 'font-monospace-input' },
        { name: 'roles', value: rule.roles },
      ]),
    ),
  );
}

async function createRule() {
  const urlPattern = document.getElementById('rule-url-pattern').value.trim();
  const roles = document.getElementById('rule-roles').value.trim();

  try {
    await apiFetch(RULE_API, { method: 'POST', body: { urlPattern, roles } });
    showToast(`已新增規則 ${urlPattern}`);
    document.getElementById('rule-form').reset();
    loadRules();
  } catch (err) {
    showToast(`新增規則失敗：${err.message}`, 'danger');
  }
}

async function updateRule(li, btn) {
  if (!requireItemValues(li, ['urlPattern', 'roles'])) return;
  const urlPattern = itemValue(li, 'urlPattern');
  const roles = itemValue(li, 'roles');

  await withLoading(btn, async () => {
    try {
      await apiFetch(`${RULE_API}/${li.dataset.id}`, {
        method: 'PUT',
        body: { urlPattern, roles },
      });
      showToast(`規則 ${urlPattern} 已更新`);
      loadRules();
    } catch (err) {
      showToast(`更新規則失敗：${err.message}`, 'danger');
    }
  });
}

async function deleteRule(li, btn) {
  const urlPattern = itemValue(li, 'urlPattern');
  const ok = await confirmDialog(`確定要刪除規則 ${urlPattern} 嗎？`, {
    title: '刪除權限規則',
    confirmText: '刪除',
  });
  if (!ok) return;

  // 確認後才進入載入中狀態
  await withLoading(btn, async () => {
    try {
      await apiFetch(`${RULE_API}/${li.dataset.id}`, { method: 'DELETE' });
      showToast(`已刪除規則 ${urlPattern}`);
      loadRules();
    } catch (err) {
      showToast(`刪除規則失敗：${err.message}`, 'danger');
    }
  });
}

// ================================
// 頁面載入：綁定事件並載入資料
// ================================

document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuthOrRedirect()) return;

  // 必填檢查交給 onFormSubmit 的 validateForm（依 HTML 的 required 屬性）
  onFormSubmit(document.getElementById('role-form'), createRole);
  onFormSubmit(document.getElementById('rule-form'), createRule);
  onListAction('role-list', { update: updateRole, delete: deleteRole });
  onListAction('rule-list', { update: updateRule, delete: deleteRule });

  // 先顯示載入中，資料回來後由各自的 load 函式換掉
  ['user-roles-container', 'role-list', 'rule-list'].forEach((id) =>
    showLoading(document.getElementById(id)),
  );
  loadRules();
  reloadRolesAndUsers(); // 用戶總覽需要 allRoles，所以先載角色再載用戶
});

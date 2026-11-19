// 用戶列表頁：列出、新增、刪除用戶（編輯在 user.html）

async function getAllUsers() {
  try {
    renderUserTable(await apiFetch('/api/users'));
  } catch (err) {
    alert(`載入用戶失敗：${err.message}`);
  }
}

function renderUserTable(users) {
  const container = document.getElementById('user-table');
  if (!users || users.length === 0) {
    container.innerHTML = '<p class="text-secondary mb-0">目前沒有用戶資料</p>';
    return;
  }

  const rows = users
    .map(
      (user) => `
      <tr>
        <td class="text-secondary">${escapeHtml(user.id)}</td>
        <td class="fw-semibold">${escapeHtml(user.username)}</td>
        <td>${escapeHtml(user.fullName)}</td>
        <td>${escapeHtml(user.phone)}</td>
        <td>${escapeHtml(user.email)}</td>
        <td class="text-end text-nowrap">
          <a class="btn btn-sm btn-outline-primary" href="/user/user.html?id=${encodeURIComponent(user.id)}">編輯</a>
          <button type="button" class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${escapeHtml(user.id)}">刪除</button>
        </td>
      </tr>`,
    )
    .join('');

  container.innerHTML = `
    <table class="table table-hover align-middle mb-0">
      <thead>
        <tr><th>ID</th><th>帳號</th><th>姓名</th><th>電話</th><th>Email</th><th class="text-end">操作</th></tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>`;
}

// 新增表單的角色改用勾選框（資料來自 /api/roles），不必再手動輸入角色 id
async function loadRoleOptions() {
  try {
    renderRoleCheckboxes(document.getElementById('new-roles'), await apiFetch('/api/roles'));
  } catch (err) {
    alert(`載入角色失敗：${err.message}`);
  }
}

async function createUser() {
  const form = document.getElementById('create-user-form');
  const user = {
    ...readFields({
      username: 'new-username',
      password: 'new-password',
      fullName: 'new-fullName',
      phone: 'new-phone',
      email: 'new-email',
    }),
    enabled: document.getElementById('new-enabled').value === 'true',
    roleIds: getCheckedRoleIds(document.getElementById('new-roles')),
  };

  try {
    await apiFetch('/api/users', { method: 'POST', body: user });
    alert('新增成功');
    form.reset();
    getAllUsers();
  } catch (err) {
    alert(`新增失敗：${err.message}`);
  }
}

async function deleteUser(id) {
  if (!confirm(`確定要刪除用戶 ${id} 嗎?`)) return;
  try {
    await apiFetch(`/api/users/${id}`, { method: 'DELETE' });
    alert('刪除成功');
    getAllUsers();
  } catch (err) {
    alert(`刪除失敗：${err.message}`);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuthOrRedirect()) return;

  getAllUsers();
  loadRoleOptions();

  document.getElementById('create-user-form').addEventListener('submit', (e) => {
    e.preventDefault();
    createUser();
  });

  // 事件委派：監聽器掛在外層容器，表格重新渲染後依然有效
  document.getElementById('user-table').addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action="delete"]');
    if (btn) deleteUser(btn.dataset.id);
  });
});

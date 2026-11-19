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
    container.innerHTML = '<p>沒有用戶資料</p>';
    return;
  }

  const rows = users
    .map(
      (user) => `
      <tr>
        <td>${escapeHtml(user.id)}</td>
        <td>${escapeHtml(user.username)}</td>
        <td>${escapeHtml(user.fullName)}</td>
        <td>${escapeHtml(user.phone)}</td>
        <td>${escapeHtml(user.email)}</td>
        <td>
          <button type="button" data-action="delete" data-id="${escapeHtml(user.id)}">刪除</button>
          <a href="/user/user.html?id=${encodeURIComponent(user.id)}">更改</a>
        </td>
      </tr>`,
    )
    .join('');

  container.innerHTML = `
    <table border="1">
      <thead>
        <tr><th>ID</th><th>Username</th><th>Full Name</th><th>Phone</th><th>Email</th><th>Action</th></tr>
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

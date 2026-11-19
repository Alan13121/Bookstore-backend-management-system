// 用戶列表頁：列出、新增、刪除用戶（編輯在 user.html）

async function getAllUsers() {
  const container = document.getElementById('user-table');
  showLoading(container);
  try {
    renderUserTable(await apiFetch('/api/users'));
  } catch (err) {
    container.innerHTML = '<p class="text-danger mb-0">用戶載入失敗</p>';
    showToast(`載入用戶失敗：${err.message}`, 'danger');
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
          <button type="button" class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${escapeHtml(user.id)}" data-username="${escapeHtml(user.username)}">刪除</button>
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
    showToast(`載入角色失敗：${err.message}`, 'danger');
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
    showToast(`已新增用戶 ${user.username}`);
    form.reset();
    getAllUsers();
  } catch (err) {
    // 409 = 用戶名重複，把訊息標在帳號欄位下方
    if (err.status === 409) {
      setFieldError(document.getElementById('new-username'), err.message);
    } else {
      showToast(`新增失敗：${err.message}`, 'danger');
    }
  }
}

async function deleteUser(btn) {
  const { id, username } = btn.dataset;
  const ok = await confirmDialog(`確定要刪除用戶 ${username} 嗎？刪除後無法復原。`, {
    title: '刪除用戶',
    confirmText: '刪除',
  });
  if (!ok) return;
  // 確認後才進入載入中狀態
  await withLoading(btn, async () => {
    try {
      await apiFetch(`/api/users/${id}`, { method: 'DELETE' });
      showToast(`已刪除用戶 ${username}`);
      getAllUsers();
    } catch (err) {
      showToast(`刪除失敗：${err.message}`, 'danger');
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuthOrRedirect()) return;

  getAllUsers();
  loadRoleOptions();

  onFormSubmit(document.getElementById('create-user-form'), createUser);

  // 事件委派：監聽器掛在外層容器，表格重新渲染後依然有效
  document.getElementById('user-table').addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action="delete"]');
    if (btn) deleteUser(btn);
  });
});

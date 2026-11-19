// 用戶編輯頁：載入角色清單與用戶資料，送出時以 PUT 部分更新（UserUpdateRequest）

function fillUserForm(user, roles) {
  document.getElementById('user-id').textContent = user.id;
  document.getElementById('username').value = user.username || '';
  document.getElementById('fullName').value = user.fullName || '';
  document.getElementById('phone').value = user.phone || '';
  document.getElementById('email').value = user.email || '';
  document.getElementById('enabled').value = String(user.enabled);
  document.getElementById('current-roles').textContent =
    (user.roles || []).map(roleLabel).join(', ') || '無';
  renderRoleCheckboxes(document.getElementById('roles'), roles, user.roles || []);
}

async function updateUser(userId) {
  const updatedUser = {
    ...readFields({ fullName: 'fullName', phone: 'phone', email: 'email' }),
    enabled: document.getElementById('enabled').value === 'true',
    roleIds: getCheckedRoleIds(document.getElementById('roles')),
  };

  try {
    await apiFetch(`/api/users/${userId}`, { method: 'PUT', body: updatedUser });
    alert('更新成功');
    location.href = '/user/users.html';
  } catch (err) {
    alert(`更新失敗：${err.message}`);
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  if (!checkAuthOrRedirect()) return;

  const userId = getQueryParam('id');
  if (!userId) {
    location.href = '/user/users.html';
    return;
  }

  try {
    // 兩個請求互不相依，用 Promise.all 同時送出，比一個等一個快
    const [roles, user] = await Promise.all([
      apiFetch('/api/roles'),
      apiFetch(`/api/users/${userId}`),
    ]);
    fillUserForm(user, roles);
  } catch (err) {
    alert(`載入用戶失敗：${err.message}`);
    location.href = '/user/users.html';
    return;
  }

  document.getElementById('edit-user-form').addEventListener('submit', (e) => {
    e.preventDefault();
    updateUser(userId);
  });
});

const apiBase = "/api/roles/mappings";
const roleApiBase = "/api/roles";
const userApiBase = "/api/users";

// === 公用 Header ===
const headers = authHeader();

// === 全域角色清單（給角色總覽使用）===
let allRoles = [];

// ================================
// 權限規則管理區塊
// ================================

// 新增規則
document.getElementById("roleForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const urlPattern = document.getElementById("urlPattern").value.trim();
  const roles = document.getElementById("roles").value.trim();

  if (!urlPattern || !roles) {
    alert("請完整輸入！");
    return;
  }

  const res = await fetch(apiBase, {
    method: "POST",
    headers,
    body: JSON.stringify({ urlPattern, roles })
  });

  if (res.ok) {
    alert("新增成功");
    document.getElementById("roleForm").reset();
    loadRules();
  } else {
    alert("新增失敗");
  }
});

// 載入規則
async function loadRules() {
  const res = await fetch(apiBase, { headers });
  if (!res.ok) {
    alert("載入規則失敗");
    return;
  }

  const data = await res.json();
  const list = document.getElementById("rulesList");
  list.innerHTML = "";

  data.forEach(rule => {
    const li = document.createElement("li");
    li.innerHTML = `
      <input value="${rule.urlPattern}" id="url-${rule.id}">
      <input value="${rule.roles}" id="roles-${rule.id}">
      <button onclick="updateRule(${rule.id})">更新</button>
      <button onclick="deleteRule(${rule.id})">刪除</button>
    `;
    list.appendChild(li);
  });
}

// 更新規則
async function updateRule(id) {
  const urlPattern = document.getElementById(`url-${id}`).value.trim();
  const roles = document.getElementById(`roles-${id}`).value.trim();

  if (!urlPattern || !roles) {
    alert("請完整輸入！");
    return;
  }

  const res = await fetch(`${apiBase}/${id}`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ urlPattern, roles })
  });

  if (res.ok) {
    alert("更新成功");
    loadRules();
  } else {
    alert("更新失敗");
  }
}

// 刪除規則
async function deleteRule(id) {
  const res = await fetch(`${apiBase}/${id}`, {
    method: "DELETE",
    headers
  });
  if (res.ok) {
    alert("刪除成功");
    loadRules();
  } else {
    alert("刪除失敗");
  }
}

// ================================
// 角色管理區塊
// ================================

// 新增角色
document.getElementById("roleForm2").addEventListener("submit", async (e) => {
  e.preventDefault();

  const name = document.getElementById("roleName").value.trim();
  if (!name) {
    alert("請輸入角色名稱！");
    return;
  }

  const res = await fetch(roleApiBase, {
    method: "POST",
    headers,
    body: JSON.stringify({ name })
  });

  if (res.ok) {
    alert("新增角色成功");
    document.getElementById("roleForm2").reset();
    await loadRoleList(); // 預設載入角色清單用在列表
    await loadAllUsers(); // 同步更新角色總覽
  } else {
    alert("新增角色失敗");
  }
});

// 載入角色列表（for 列表）
async function loadRoleList() {
  const res = await fetch(roleApiBase, { headers });
  if (!res.ok) {
    alert("載入角色失敗");
    return;
  }

  const data = await res.json();
  allRoles = data; // 更新全域角色清單

  const list = document.getElementById("rolesList");
  list.innerHTML = "";

  data.forEach(role => {
    const li = document.createElement("li");
    li.innerHTML = `
      <input value="${role.name}" id="name-${role.id}">
      <button onclick="updateRole(${role.id})">更新</button>
      <button onclick="deleteRole(${role.id})">刪除</button>
    `;
    list.appendChild(li);
  });
}

// 更新角色名稱
async function updateRole(id) {
  const name = document.getElementById(`name-${id}`).value.trim();
  if (!name) {
    alert("請輸入角色名稱！");
    return;
  }

  const res = await fetch(`${roleApiBase}/${id}`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ name })
  });

  if (res.ok) {
    alert("更新角色成功");
    await loadRoleList();
    await loadAllUsers();
  } else {
    alert("更新角色失敗");
  }
}

// 刪除角色
async function deleteRole(id) {
  const res = await fetch(`${roleApiBase}/${id}`, {
    method: "DELETE",
    headers
  });

  if (res.ok) {
    alert("刪除角色成功");
    await loadRoleList();
    await loadAllUsers();
  } else {
    alert("刪除角色失敗");
  }
}

// ================================
// 全部用戶角色總覽區塊
// ================================

// 載入所有用戶 + 勾選角色 checkbox
async function loadAllUsers() {
  try {
    const res = await fetch(userApiBase, { headers });
    if (!res.ok) throw new Error(await res.text());
    const users = await res.json();

    const container = document.getElementById("userRolesContainer");
    container.innerHTML = "";

    users.forEach(user => {
      const section = document.createElement("div");
      section.style.border = "1px solid #ccc";
      section.style.marginBottom = "10px";
      section.style.padding = "10px";

      // 標題：顯示用戶名稱
      const header = document.createElement("h3");
      header.textContent = `👤 ${user.username}`;
      section.appendChild(header);

      // 顯示現有角色（english排序）
      const roleText = document.createElement("p");
      const currentRoles = user.roles?.slice().sort().join(", ") || "無角色";
      roleText.textContent = `現有角色: ${currentRoles}`;
      section.appendChild(roleText);


      // 顯示說明文字
      const hint = document.createElement("p");
      hint.textContent = "勾選下列選項，以更改用戶角色：";
      section.appendChild(hint);

      const checkboxArea = document.createElement("div");
      allRoles.forEach(role => {
        const label = document.createElement("label");
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.value = role.id;
        checkbox.checked = user.roleIds?.includes(role.id);
        checkbox.dataset.userId = user.id;

        label.appendChild(checkbox);
        label.append(" " + role.name);
        checkboxArea.appendChild(label);
        checkboxArea.appendChild(document.createElement("br"));
      });
      section.appendChild(checkboxArea);

      const saveBtn = document.createElement("button");
      saveBtn.textContent = "保存角色變更";
      saveBtn.onclick = () => saveUserRoles(user.id, checkboxArea);
      section.appendChild(saveBtn);

      container.appendChild(section);
    });

  } catch (err) {
    console.error("載入用戶清單失敗", err);
    alert("無法載入用戶清單");
  }
}

// 儲存單一使用者角色變更
async function saveUserRoles(userId, checkboxContainer) {
  const roleIds = Array.from(checkboxContainer.querySelectorAll("input[type='checkbox']:checked"))
    .map(cb => parseInt(cb.value));

  const updatedUser = { roleIds };

  try {
    const res = await fetch(`${userApiBase}/${userId}`, {
      method: 'PUT',
      headers: { ...headers, 'Content-Type': 'application/json' },
      body: JSON.stringify(updatedUser)
    });

    if (!res.ok) throw new Error(await res.text());
    alert(`用戶 ${userId} 角色更新成功`);
    console.log(`User ${userId} 更新角色為：`, roleIds);
  } catch (err) {
    alert(`更新失敗：${err}`);
    console.error(err);
  }
}

// ================================
// 頁面載入時
// ================================
document.addEventListener('DOMContentLoaded', () => {
  loadRules();       // 規則管理
  loadRoleList();    // 角色名稱管理 + 更新 allRoles[]
  loadAllUsers();    // 全部用戶角色分配
});

// 權限檢查
async function canAccess(url, userRoles, urlRoleMapping) {
    try {
    const mappings = urlRoleMapping;
    for (const mapping of mappings) {
        if (new RegExp(mapping.urlPattern).test(url)) {
        const allowedRoles = mapping.roles.split(",");
        if (userRoles.some(r => allowedRoles.includes(r))) {
            return true;
        }
        }
    }
    return false;
    } catch (e) {
    console.error("檢查權限錯誤", e);
    return false;
    }
}

// 獲得後臺權限清單
async function getMappingList() {
    try {
    const res = await fetch("/api/roles/mappings/public", { headers: authHeader() });
    if (!res.ok) {
    console.error("錯誤：" + res.status);
    return null;
    }

    const mappings = await res.json();
    return mappings;

    } catch (e) {
    console.error("無法獲取訪問規則", e);
    return null;
    }
}

// 動態產生選單按鈕
async function addMenuItemIfAuthorized({ url, label, page }, userRoles, menu, urlRoleMapping) {
    if (await canAccess(url, userRoles, urlRoleMapping)) {
    const li = document.createElement("li");
    const link = document.createElement("a");
    link.textContent = label;
    link.href = page;
    li.appendChild(link);
    menu.appendChild(li);
    }
}

// 登出
function logout() {
    localStorage.removeItem("token");
    alert("已登出");
    location.reload();
}

// 初始化選單
async function initMenu() {
    await refreshToken();
    const menu = document.getElementById("menu");
    menu.innerHTML = ""; // 清空

    const token = localStorage.getItem("token");
    if (!token) {
    // 未登入
    menu.innerHTML = `
        <li><a href="/auth/login.html">🔐 登入</a></li>
        <li><a href="/auth/register.html">👥 註冊</a></li>
    `;
    return;
    }

    const user = decodeToken(token);
    if (!user || !user.roles) {
    // Token 無效
    localStorage.removeItem("token");
    menu.innerHTML = `
        <li><a href="/auth/login.html">🔐 登入</a></li>
        <li><a href="/auth/register.html">👥 註冊</a></li>
    `;
    return;
    }

    const userRoles = user.roles.map(r => r.replace(/^ROLE_/, ""));

    // 已登入，加入登出按鈕
    menu.innerHTML = `<button id="logoutBtn">🚪 登出</button>`;
    document.getElementById("logoutBtn").onclick = logout;

    // 定義選單項目
    const menuItems = [
    { url: "/book/", label: "📖 書籍管理", page: "/book/books.html" },
    { url: "/user/", label: "👤 用戶管理", page: "/user/users.html" },
    { url: "/api/roles/", label: "🛡️ 角色權限管理", page: "/user/roles.html" },
    { url: "/a_office/", label: "A部門", page: "/a_office/a_office.html" },
    { url: "/b_office/", label: "B部門", page: "/b_office/b_office.html" },
    { url: "/c_office/", label: "C部門", page: "/c_office/c_office.html" }
    ];

    const urlRoleMapping = await getMappingList();

    // 檢查權限並顯示可見的按鈕
    for (const item of menuItems) {
    await addMenuItemIfAuthorized(item, userRoles, menu, urlRoleMapping);
    }
}

// 保證每次進入頁面（含上一頁 BFCache）都會刷新。
window.addEventListener("pageshow", function (event) {
    initMenu();
});


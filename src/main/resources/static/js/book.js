
document.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(location.search);
  const id = params.get('id');
  if (!id) {
    alert('缺少書籍 ID');
    window.location.href = "/book/books.html"; 
    return;
  }

  // 載入書籍資料
  fetch(`/api/books/${id}`, { headers: authHeader() })
    .then(res => {
      if (!res.ok) throw new Error('找不到書籍');
      return res.json();
    })
    .then(book => {
      document.getElementById('book-id').textContent = book.id;
      document.getElementById('book-title').value = book.title;
      document.getElementById('book-author').value = book.author;
      document.getElementById('book-desc').value = book.description;
      document.getElementById('book-list-price').value = book.listPrice;
      document.getElementById('book-sale-price').value = book.salePrice;
    })
    .catch(err => {
      alert(err.message);
      window.location.href = "/book/books.html"; 
    });

  // 表單提交處理
  document.getElementById('edit-book-form').addEventListener('submit', e => {
    e.preventDefault();

    const updatedBook = {
      title: document.getElementById('book-title').value,
      author: document.getElementById('book-author').value,
      description: document.getElementById('book-desc').value,
      listPrice: parseFloat(document.getElementById('book-list-price').value),
      salePrice: parseFloat(document.getElementById('book-sale-price').value)
    };

    fetch(`/api/books/${id}`, {
      method: 'PUT',
      headers: {
        ...authHeader(),
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updatedBook)
    })
      .then(res => {
        if (!res.ok) throw new Error('更新失敗');
        return res.json();
      })
      .then(() => {
        alert('更新成功');
        window.location.href = "/book/books.html";
      })
      .catch(err => alert(err.message));
  });
});

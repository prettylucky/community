$(function(){
	//捕捉发布请求按钮的单击事件
	$("#publishBtn").click(publish);
});

function publish() {

	// 在发送AJAX请求之前，将CSRF令牌设置到请求的消息头中。
	// let token = $("meta[name='_csrf']").attr("content");
	// let header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e, xhr, options) {
	// 	//下次发ajax请求在请求头中携带token
	// 	xhr.setRequestHeader(header, token);
	// });

	//获取HTML收集到的帖子参数
	let title = $("#recipient-name").val();
	let content = $("#message-text").val();
	//发送异步请求，把帖子信息提交到服务端
	$.post(
		//提交路径
		CONTEXT_PATH + "/discuss/add",
		//提交参数 json格式
		{"title":title,"content":content},
		//回调函数，浏览器会把服务器返回的参数(json)传入
		function (data) {
			//解析json字符串为JS对象
			data = $.parseJSON(data);
			//将服务器返回信息，写入提示框
			$("#hintBody").text(data.msg);

			//隐藏发布帖子页面
			$("#publishModal").modal("hide");

			//显示发布帖子结果
			$("#hintModal").modal("show");

			//两秒后消失
			setTimeout(function(){
				$("#hintModal").modal("hide");

				//刷新页面，更新帖子
				window.location.reload();
			}, 2000);


		}
	);


}
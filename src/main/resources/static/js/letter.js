$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

//使用ajax发送异步请求
function send_letter() {
	//获取参数
	let content = $("#message-text").val();
	let targetUsername = $("#recipient-name").val();

	$.post(
		//发送路径
		CONTEXT_PATH + "/letter/send",
		//携带参数
		{"content":content,"targetUsername":targetUsername},
		//回调函数
		function (data) {
			data = $.parseJSON(data);
			//设置提示信息
			$("#hintBody").text(data.msg);
			$("#sendModal").modal("hide");
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				window.location.reload();
			}, 2000);
		}
	);


}

function delete_msg() {
	//获取参数
	let btn = this;
	let id = $(btn).prev().prev().val();
	let conversationId = $(btn).prev().val();
	$.post(
		CONTEXT_PATH + "/letter/delete",
		{"id":id,"conversationId":conversationId},
		function () {
			// TODO 删除数据
			$(this).parents(".media").remove();
			window.location.reload();
		}
	);

}
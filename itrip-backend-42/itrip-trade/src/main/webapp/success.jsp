<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<body>

<h2>支付成功!</h2>

<div>酒店名：${hotelName}</div>
<div>房间类型：${roomId}</div>
<div>支付宝交易号：${trade_no}</div>
<div>付款金额：${total_amount}</div>
<div>入住时间为：${checkInDate} → ${checkOutDate} </div>
<div><a href="${index}">跳回首页</a></div>

</body>
</html>

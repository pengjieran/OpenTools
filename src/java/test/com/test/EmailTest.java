package test;

import com.opentools.common.EmailUtils;
import com.opentools.mail.EmailConst;


public class EmailTest {

	public static void main(String[] args) {
		
		EmailUtils.send(EmailConst.SMTP_163, "发送邮箱账号", "发送邮箱密码", "目的邮箱", "主题", "内容");
	}

}

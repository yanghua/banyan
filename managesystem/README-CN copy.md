13.07.01

cd ${ofbiz_project_base_path}

ant load-seed

ant load-demo

open url : https://localhost:8443/banyan/control/main

会员-》会员选项卡->新建-》新建人员->填写必要信息（名字、姓氏）-》新建用户登录-》点击安全组-》选择BANYANADMIN
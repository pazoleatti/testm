set long 32767
set pagesize 0
set linesize 4000
set feedback off
set echo off
set verify off
set trims on
set heading off
--set termout off
set serveroutput on size 1000000;
spool &1;

delete from plan_table;

explain plan for SELECT DISTINCT operation_id FROM ndfl_person_income WHERE operation_id IN 
('a7083a89-6d48-4c7a-bd6e-c9888fcd57c7','38faba9f-0fc1-40e0-8552-40cece6058e2','57eb67d8-f75f-4379-b71d-3c0aadfcc8b9','8f2657cd-f883-4fbc-bde9-c2b232ad0d77','b76fc6ac-9ebe-4462-aa2f-1b16ecf22ec9','3daf004d-d4b7-47ef-9059-ed0b5f7658d8','6b774588-210d-4b0d-8361-54bf99d6db8c','978ed11e-23c3-4f5b-8466-dbf08f978452','999ba611-8837-4dc0-8b60-9d82263ceb7a','796a0aa1-d8dc-4c90-9e03-053c4840ad72','290440f7-db73-4cbd-a97e-b9f56e50313d','fcff90ae-774f-4535-8c8e-df29420188fa','16203baa-0bd8-44dc-b3a9-3e55ef3a88f9','5782733c-69a2-4d99-886b-028020fbe922','aeda4421-9197-40b2-b22d-34fdd63593d5','f9e10c5a-2565-4b56-aaba-09da5ba80403','1ab8139c-341b-4e23-99ab-dc9412386d8d','2d9bab0d-30e2-4174-882a-5306c051952c','f22776ce-7a58-46df-9c67-e2a40066ee2f','253c6fd8-955e-4be7-8eb6-6b1c5168c4cf','7616ddc5-0edc-47c5-8510-4efbf5fa1f6c','a845e556-e476-4ef0-be1b-40edf0acc4ee','5713f2af-bad2-4908-8939-5c680cc3ed63','31033f43-bde7-4e68-8bf8-433a9c8ee07c','6ef89068-87ba-4c58-83b2-cd34a2c4b75f','990eb1ad-9a6d-4cfe-8b59-1043d2fb9b73','d4043275-e479-4c07-a1cf-e0fd1f0ac5a4','f258131f-d144-41c4-8da9-6e1ef09011ee','5cc377e1-1c02-452f-be9b-2cdbfe7058b8','0d129a24-52a6-4365-9f71-1e8483eaf7f7','93dbbb16-7344-49ca-957f-87ffec4f0835','24efc467-ebd9-4353-bcdc-aabf281db019','aae9c8b3-bb29-480f-a353-26b17324737e','662ddd61-a5d2-4cb4-a322-656d72da6838','fb3cf5dc-6e08-443b-b488-5177de3c4561','515ed28c-625f-4a7d-9810-60644495360c','4b85e0c0-d3c6-40c9-bc11-c60af69a0010','4a622d37-bc81-4528-bf58-4cda282a8623','e6091c7d-1076-4422-87c0-73a0109ac46f','ba4db9a3-7a18-4757-abf5-37c4045d2bc6','2d83acc8-6418-4c33-9f6d-cc9c44293011','a2df199c-fb80-4679-9aa1-952b9eb06e96','75776824-c7c7-4cad-b572-eb4560c1652a','0ddde7f6-e30a-4793-9dd0-99c150d9f8f7','239d5049-d7f9-48bc-a054-2d3c833e5df8','93129909-f38f-467a-af0f-1c7b0adbf921','247ed31d-30d9-4e0d-974e-f1ec72602ee9','63b975a7-ba43-454c-87d9-baf62cee4921','341279bd-bb21-4609-9975-6cc58261634e','9baf13d3-62c7-4349-b4e0-7d2066f4948f',
'2e76a839-8a3f-4f62-82d6-1fb97ebc17ea','ede91a3b-bad0-49bf-a8cf-7348e66c6d90','b00f9092-3f19-461e-a0cc-1b77d5b47b86','89ec9759-2cc5-4fef-bc86-e5d9e987239a','1fc7b778-689b-40c9-b6c3-18b494ae7f95','5e374413-64ac-436e-9a8f-d06893bb8945','8a19bdb9-a226-4daa-aa1d-b3872552612a','f9a26c52-b8cf-4528-a7ee-a1a6d2a08822','09a92add-9e93-46a6-93dd-9321ac428a04','c59d39bf-75a4-489c-8336-345757913a30','76125dd4-5b26-4c03-b68b-f4d208eebd77','2494aa67-c081-4f4b-a096-c0e988c50068','6d8d2861-f345-451d-b4a4-fdd77af56ae9','fe869e35-2666-44f4-b307-261dca90947a','2017ebca-4b47-486e-aeec-36df2398f7e4','56eb748d-17a2-429f-b24a-336a95044086','76235ad0-d060-4e29-9336-bf0a30cef338','407371ae-3618-463a-ad4f-2be772c9933c','dd6bd871-eb1c-4fe2-8e6e-c59dbf150cd6','34f66d7b-f6fa-47fb-b0e6-ab9d2204cd82','708d452c-411b-4c8c-afb3-30ad99a93421','708c1a35-1249-42f6-bd64-1191e9d01a80','562dfb4b-6147-465a-acc5-7dad26d65f2d','19647044-afb3-4f04-bc2e-6232d75b2cfe','22e12041-cd7b-43c8-a31e-4d025a800cd0','d83eb7c0-7c34-42ec-adda-b53d3b5201f3','6c230142-7572-454d-b5f3-64212908ec47','a7d21d8e-af2e-4617-ad7f-c1b0b1ab4d28','8a9343f7-5ec6-4a66-b337-4942bd2cd2ba','a6b5a5c5-01db-42f9-b34d-6786dc2c8bc6','31513c3a-9663-4787-a35a-9477f21fe1f4','cef467d0-afa8-4d71-9977-116e5d1121ae','1e8ad7b1-3959-4867-8766-633e8ef0fd5e','b9b996f0-83a0-425f-8d43-efe28f97d2b7','7810f065-1e80-4b6d-b568-7eb4cfab9d97','900b4880-7d41-4791-9057-c0d3f4f3c34c','24c4faa2-c205-47d5-b775-d665ec9644ff','564b977d-7ae6-41b7-a023-c68b6bc5ca18','09910239-1313-4a62-8a89-f7e75115cec0','c1f71192-6fd6-4da5-a5dc-74ce8dda2eef','bc63d66e-065b-4c62-bc45-67a563e08239','5e6e8a21-4dc8-4ef8-8bd6-311c38b011e5','7eb54abd-94c5-4aa7-8eea-1431adc751a0','890570c2-7c2b-432c-b092-df314bc691ab','f214b683-5bdf-48d9-b9cc-b0726d673820','4f1e1897-2549-4b5e-ab1c-f5e2f3fc0bdd','9fa22364-84e1-4856-b384-8d70f5c3610c','33ce8655-070b-416b-af91-e52dcf96166b','07b39daf-0114-4466-a8db-53cca4d767b3','e8a02437-7f9e-4830-9630-49f20f01a871','08a30b18-e1ec-4997-848c-79eee1d61161','9ff92401-f5c5-4ef1-8f21-54119cd3e724','59601c48-a6cd-4f20-bcab-cd5ab240f5d1','fdd93a32-7147-41ab-90ad-78f74eb2ca34','c63cc270-afbc-4000-89a1-0608fccaa0e3','0f2f7430-73e9-4417-84ab-f4f8884573bb','97b21581-f17b-4291-8c0d-8f3f4a63ff48','88470976-feab-4f50-9a76-cc273cb0a21a','fff8fbb8-46c8-43dc-b9b8-fa53f73e8a86','07dfa5b9-1516-4f51-81cc-086b6d535323','fd5c1322-48d8-4742-9cec-8a677de87194','05afe335-6ad6-4552-9640-b863de74b87a','f7d1369b-9031-4cc2-a10e-d2f436d1b471',
'794ece70-ab22-4fe5-b097-dda8ec321ab6','d5197f0a-fecc-415a-b8d4-9cdfde4daf63','2b9eb545-37e7-44ee-abd8-ff3948fa81ab','a9947c8e-f1db-473d-84b0-f9334d05f252','71c0d633-8992-4864-9457-6629b2c17186','4fe46379-b377-42f5-88a4-75ee7fd20a19','7f52d2e1-8d4f-4d0a-8276-0e56b32e8de8','647d1865-2385-4d9d-9c90-fbb4d3615686','5870c2fd-a150-4f44-adce-ead058abd61d','ae2c46ef-57ef-4bae-90ea-9c8b0def1fbe','125db931-6124-4b63-a238-c14b86b382aa','d18cdcb1-eacd-4474-bfc2-cd92713f2d65','9afe139b-08a3-4ef2-9876-4039c1da5584','675f65f8-4a91-44b4-8ab2-820e297fb907','656d0f49-f54c-4029-a2a9-1c375eb424c0','97aae3c7-1676-4696-8a4b-7c07c471445b','80ecbb94-9d86-454d-855d-709a8a958abf','09d382c8-dc52-4c1c-a940-aaef80fafdd4','fa381e25-6dc8-4a59-bae7-bb67801d896c','d9ea6ef2-f89a-4797-af43-46230f0c9616','c54c6d84-9d9d-43a9-abb8-346d670ed1a4','8c980d7c-bc0f-44b1-aa76-c23e419eb445','83565fbd-0ae6-48f3-9241-6a2d029e6fc7','e5dde375-ce04-4139-8d98-103c0778d607','56e31a5d-d399-4c43-a504-1775e214a734','b03392ad-2c90-41d5-ac2f-b8dbbcead5de','c0e2c61a-578c-4038-9682-7168f2e9dd8b','f5de841d-ac99-4b29-b657-673f95074d33','6bbbd31d-0237-4d53-b8f9-ddaac55494a7','1b3c4383-f872-47e4-bd8d-da074fc6894b','c813ea55-6c5d-4881-9519-1ff4f5fd8900','29e14022-fd15-48ef-ac20-49749ec5630c','418f4feb-a3fd-4629-9217-b06ef68a6398','2a2e57b7-bd13-4edb-ba39-0df63296f83e','dab102ba-5ae8-41e8-b476-04a04686eabb','ccb705fc-687a-4186-b752-ecc896a4622f','7393b8ba-186d-45aa-9855-6ab5f49dd05f','741abd78-d1cb-4c1b-9b43-fd4eff71bf9c','ff3ae100-3723-4b9b-9d18-b46014e89abf','0f4756ea-751a-4eef-b04a-9f4f30aee963','eeeb4ce4-8a61-4165-b0c0-2e840b0d9e56','11ef27ef-1757-4bbd-b2a1-c63d22744af9','0953f5a4-caf5-47d1-9222-5370fe8ff788','cf21bd2f-9737-4f6d-b316-33a30c47339f','3fa65326-fbe0-4cfe-933f-6967fb6842b4','cfeb2f88-ab26-4d1c-844f-7c711363ef46','88e451e6-57ce-4cff-9667-377eab05b6c8','f15c4ee1-32da-468b-b6a9-3708be02e438','6ae6a0a9-8a2e-44ad-932a-3a8cf2420fa8','dcfdcbb1-64d9-4a3f-8627-0a89b48d1fde','f285278e-584b-43a8-ba73-daba9c7bd07f','76a9dbb3-e8ef-4c51-bc0c-72764afca51b','dbe69a0b-dca6-4939-9f33-b676254a248f','8c681d18-e501-4141-b723-3a1a39fa1a79','ad8baadc-8ec9-45f4-911a-2619332d5911','59add020-ff8b-4b22-a7b1-390f2b734e84','4d721547-0d06-4882-9e09-0d8d68805e60','85ae8474-3771-49e3-9c11-fced328f8126','dcb63f3a-7c09-4ea9-b78e-5890e3127c1c','1d7dbfed-ae00-45ae-ae69-95f3a1e84f13','576ed19d-247b-4845-b0ef-b41efcfb0204','df51047c-8ffa-4e94-910a-4cc9c3484c27','7805ef89-37f1-4482-807b-9403c0ef1d7c',
'825d5aae-afdb-470d-ae32-687044b0a2e1','20a65467-af84-436a-8065-b4192ad16ce3','81918291-6595-4175-a988-176214f08698','8703329c-fa61-4d97-bebb-76c4c2b4c574','62cd78dc-6030-4570-bdd5-b7ac1377d4cd','329868fd-9d2e-4818-a518-2a0606ff5878','961ddfa4-3a44-4015-9c3d-aa51a051e55e','85309a5a-c88c-4d64-9c94-68ad26f5360c','3ceff946-bc49-4937-b31c-7838f80c848e','23e9c0b8-47bb-4842-9a9c-d03ab671af9f','1448c785-12ae-48b0-b554-3dfe08053c4d','e3cff051-d896-46fe-a0ce-878f43fba2ec','0682fb06-2f66-497e-a439-528541bd072c','fb5b66ed-f3f7-4b80-a668-a482b1dabcde','c7e1b698-e534-4ef4-95e3-d90fff75dfb4','731f2ee4-bd69-4d9e-b36f-1625af1f7c8c','76f02919-7d43-4576-8e67-e2ba4415d6dd','e11ed7ae-a0ed-4f36-ad10-0e019397c520','22565056-a797-49c2-bc1d-8481ff49de92','afb09eec-c4dc-437b-85e9-4852549ca800','845a6bb3-fc94-4915-9feb-9912268a0cd6','c2dc7d85-3ae1-454c-9dd5-82027f12cbf0','f74f2f61-a46d-4d9a-86ad-b4db24a2bac6','b0447e50-7ca5-4e88-a7d6-06b72e488c30','78c84128-5270-4c69-9731-3d356c8aa095','0ec3137d-e3ed-44e6-a356-081b11293ddf','f20dc330-b11c-46bd-9a2d-e54541dbcc61','b159ebf1-9ed1-4814-ab6c-e45a6dc8a640','8762e68d-e442-4e18-9016-ee172ef02dec','7a54f144-5780-4713-9c82-c508e8fe95d2','3857f314-d49c-455d-ab87-633cae56247d','d72e370a-aa10-4f64-b13a-8106c350b256','105b0134-38b1-4c40-91f7-d15a68adb1eb','b72d287e-9d5e-4142-9d2a-b084a3966229','ad7f38e9-c4b9-418e-a81d-8e4cc5828729','208f8af2-817c-4909-8b25-ac16d0df3094','26977832-8f2d-45c5-8d10-037e4565420d','3f4319cb-2b7e-4624-9d87-e0f9f392382d','a6540464-5486-443e-9715-4ddfade182d0','26f25890-4eb0-4654-889d-292e99b0e64e','06f8fe46-b2f2-4a0b-bae0-4fef15751d9a','0ed5ad19-7e33-4941-b77f-269916e37e99','d7a23eb2-53da-447d-93b6-42f3768990a2','0190f43d-4e85-4243-aa12-57b1f7908113','1fc69e52-9fea-4d02-aa7c-37b7fc1cf3c3','7b539c42-2afc-4aa8-a8b8-5181e0c40859','b904e4b1-c9e3-4218-adc3-103e286fccff','adee3e46-d7ff-432d-a8c7-a5a13cc20133','ace2ec4b-5736-471c-a3e7-af4da2d3896f','e3d3ef35-db8d-4815-865e-0cc61c2db8a5','8cb05f6d-bf8b-4be5-861f-17a969f9aeb1','930b79fc-4b62-4ab3-9e94-1855b78e9624','fb0161af-b60b-4ba3-85d4-a6ab7eb4b796','342829cc-f0c6-4361-80e0-b7eb0252d854','21c581a7-5270-466d-a888-cd7e0a35cb7c','5774aef0-1c13-47db-9ee9-5322f23ea427','2d1329e7-ca0a-44b8-b7fe-838e5e3c7c4d','ce071d7b-d138-42b0-a60f-e7a21f30ec66','760785e6-4cba-4bd2-a28a-5dddc4eaa916','0a9458b8-6221-488e-95da-dcd407871a76','d06062ef-be5d-487a-b5ab-c8d7ae767206','7687177b-6fef-40bf-b275-5aae2d6b560f','aaa609ff-14b8-40c5-a70f-e983273e0b9e',
'a23ebf5b-d74c-42bf-b947-1dcb45456e54','67a12b11-1c88-4d7b-ab93-69411053d5ce','a393336f-3a7e-4ccb-a177-d421a4bba82f','a7f795a6-4c60-437d-bc5c-dbc6f7c9f4d6','43e59341-c8f3-44df-b887-b54ad8194469','b461a9fe-c36c-4bed-b971-5dc76739fda8','dc41cbdd-4330-48d3-bbe1-425fce487acb','56af0a9e-ae49-47ca-bf27-b4856e9c7775','e3fca794-8930-4525-aa61-da6ccdf515f5','161388ed-a703-4c6b-9083-cddbebd946c7','7512032f-eeb9-470b-80d3-eabc92cee46d','7f5d540c-480f-4275-af99-b86d36a2e189','6cdd985c-b5f5-485e-86a9-3fe0298cfb86','4b4e5997-cf16-4fee-b298-4923bab1383f','8f22fd82-766a-42c5-957d-a8f9662c5368','3da88b58-1a4d-4c00-b8e7-cba809aa8538','1f094eee-f189-4a64-a3fa-39309c4c1d34','089c4557-e71c-466e-8ba7-5d9b944a080d','4ab3aa12-1c99-4f18-afdb-798798f3b646','d5efa44e-4e0e-4826-a413-7e28cf74159b','a651a6e9-fde5-467e-8752-e6afa7d08f3b','c821f266-bb8e-4547-acbd-23ba6725c0a6','3b932929-e58b-4ec5-af5e-edf611497016','566dd79e-4394-418b-bc58-8ecf481f08a3','35b99e9c-d5c7-4e81-8489-c912b5b0c100','c6f1a7e3-2004-4954-9c03-002cca2d4fab','eff8f868-0b5c-4cc6-a16b-3d1f3860d41c','4f73df36-b2a6-48e4-8bc1-d9035aab11e9','db58a037-b661-4044-a87d-fbb8e368a8b0','da664412-f9bf-4058-a855-7267e3a48eda','8743eb71-2175-4441-acde-fe28602fef51','95d58ff6-ad6a-4e50-9ef5-7e6938b358a3','1af9f831-12ad-41a8-957e-05d2e48daae1','614f32e2-5bab-4331-95b4-fecbddd443e3','0f809972-9eac-4086-be0e-8a9481bd489f','395f00d1-2d93-40f8-9f48-3da160bbc392','c241997b-7926-411b-b3dc-f5925c71e7cf','eb7a3c53-d582-4e12-bac1-6dfc3a94c569','511aef5b-e129-4e84-8f05-e0741816ba8c','7b66aa15-c8d2-4f58-bfad-5541f56a0340','de71a026-7c40-4070-9aba-ef8f19796a2b','cab2f13e-ece2-4dfb-ba93-c0d61cd2a0d1','5ce103a5-2e10-42a8-9e7f-98bbc85ab249','d99b36f7-7d9d-40df-9304-995d35aababf','529855b8-c156-454a-a8ab-42984846da59','7ad49e7d-f91c-40e7-85cf-226e71bafd3c','16113376-1a2b-49d4-a0c0-c5609fc567fa','f7594fab-7880-4408-a1ae-b29c70a4b237','e8966270-7b4d-4167-bfde-65214c6440b4','d783bb07-d79d-45a1-8d9f-b308b239c686','5a0fedde-a968-46fc-8ab3-e50ccdc7106c','775ac373-efa6-4c20-a19f-6660908a8280','7e90253f-f673-437b-879c-c06cf6f9659f','44f1e6eb-92be-4234-9469-b5f90f75c734','185bf4e9-90f0-4253-bdec-3c64c7474794','d46be71e-7ab1-4e0b-bdc3-781f9eb37a78','7f4dc8ae-c4e4-480b-94b5-7cab04447c3f','9c070e17-3d4c-47f7-8f1b-e219b8a20e96','a58e2f81-e0b7-4766-9df1-f3c5d1fe0d97','83ca0331-7c52-4d63-bc33-124f8e1cac1f','d78ae24c-f5c5-4acf-8055-e5573d85c55d','36b13b1a-6f79-47c9-91cc-c3feae4d5a7f','61c0edbb-74b2-497d-8783-36a4f03c9130',
'cab67b73-faf6-4a2a-80c9-d968257d001f','d5327c3d-6f20-43b5-8355-f0b9cbe8bdfe','cbbc94d0-681f-4d57-bf0b-41d50b37edb0','8078dbc1-d53d-43bd-81a0-d8f2f688423f','100d203a-9dbc-49b2-9723-121b9f40891c','c72c6152-a3ad-4b22-b7a0-a8a463d425d3','e9d33a8e-9981-48a3-9b6a-bb238b71cf4b','926bc32e-418e-4e1a-8ecb-0b78378d4df5','f40ac814-1eff-4d1e-84ef-bbf2517c74f9','1f43c798-3920-48f7-9070-920222a2c888','de36f49f-71e5-428f-86a1-168986d4e320','5ac91b02-a34b-4f2c-b7b9-b4a931b5c0b6','9103f0c8-e158-412b-a164-b55db56e35c5','43a52e25-e32c-4bac-ba77-34967ec33654','8f16f555-001b-44b5-b945-49772a1fbd1c','5b3b7729-f192-4afc-9f2a-a6f7e43e5cd6','42f0e15f-c5e3-4aa8-be1d-1c2dcbfa1012','32f5bbc1-2e03-44d2-a74b-bf5f6b76f21d','ec5eb58b-eae9-4edd-8c26-b86598e136f8','698b9454-39fb-4342-b907-2b2bdd8824bc','59621aee-66e7-4236-8033-8064a1fbb1da','167cf4c6-91ad-4ec5-b434-3f49390567a9','8af459a7-da76-40d0-8bbc-1b246482aa67','8bfe1d29-4744-4b4c-b87f-5b8ac7f17108','00b582a4-180b-43c7-83e7-7bcc6e272727','971b5bdc-c8bb-46ef-a459-4a5b77b918a2','672bd9b5-1214-4d0e-a995-11ed1d185df3','2018c3f2-3871-4bee-9f76-c4376f91f6bd','ed36ade6-2d22-4a0b-9e89-c0ea121fb2cd','44f75643-efed-4cdf-bbb6-18a1537bae14','89ac7606-20fb-4329-90d0-48d16b9c4cf1','3d5fbfa7-8b62-44f2-a573-b35684c72818','b26e42d1-e53a-46f7-820a-be5981f83fe9','2e8b29b7-49f1-48d0-8156-e85a84b7f4b6','59080e02-cf0b-491b-99d7-8d91853ffd4c','119ec2bf-b553-42a7-b264-1dada10b73d5','e67fe525-745e-4f27-bb43-8bf153d6afd7','b73b1946-602f-479d-8b57-323dbe6f0a01','45f5e415-0c06-4c8c-9972-79adb8f75274','c26b29e4-090f-4a43-91c7-7c72bf289078','ccea8b39-2b64-438d-a41d-05568cd13e7f','d671a1e5-cb0e-4995-be66-cad0b2b3d83c','cb554c76-6df6-4421-a53f-580d00cb8c80','4a3ccddc-8064-4c64-96b9-f03864081deb','5936c66c-3449-445c-8134-9c4a2b774287','b4412e80-2b6a-45d0-a31a-3179ca4de8b8','385135cf-b007-4954-94d3-bfbe83bdaa29','a63a4942-5bd6-4d34-b3f4-af8b25e73c79','133dd10c-ec89-41dc-b889-970c79be3af7','7531ba75-f13c-4642-8d32-39ad2f480deb','77cc5c35-a7a0-44ff-a2d8-80e3620136ad','3e8b5c4d-fdea-46b2-b142-06a5e0dda699','ed8b4146-f513-4f2b-a089-514adecc7251','7e358922-64f0-4b8e-bec2-8801ab6db20e','76e8681b-d837-4867-bec2-1d90db386287','d6089b5e-e5ef-466c-a98d-af6410877ff0','170b8435-8b99-4ab9-b433-8bb6f0b2c8ac','d15d38ad-9dd6-42bf-8100-668df55aaa5c','09136de0-a38a-45d2-9d4e-4116fac251c6','882a622d-df57-4dc0-9dc0-815368972d40','ab39d90a-7ae1-4bf7-afb6-eb700bf5e046','3dc0480e-1d76-4874-896f-1090e6238e8b','17a4fc86-4f86-4f3b-884d-31987c7c51a0',
'89ec655c-b00b-4aa5-a5dd-e6f980b99591','56341a8b-3caa-4c21-b049-de362b4529c0','c7a25d4e-0c0f-45e9-8d23-0e3724385b91','ad6536a7-24e1-49c6-b8fa-5174181dbdb1','e7af2134-c8ee-4103-a24b-88e71efb0cb2','3da42849-63a0-4639-8896-4efee91d8f9c','ac6fcaa6-b7d1-4df6-8ac5-396c57d5e989','6f7795f9-f9c5-40bd-9f9b-bcaa1fa96312','bdb03b10-61ea-4985-80af-b214c7a01741','7b69815f-b9f7-4214-a35a-2411b962a554','10a33811-e1ce-4413-9197-9f9405540237','3c942dfe-da26-46b0-90f8-ece14d7e1ab5','e0970dc2-4ac2-4b68-8841-0af26f615fc9','5e25bdc7-c01f-44b3-9b8a-6dd33cac0e25','8476d9a6-e44b-47c1-ab33-b68ab1522396','4842bb14-9616-465c-99f4-180a39aef1ce','49def410-4e8d-47a9-8534-e1decd7121c8','1b6cb838-5967-436c-b950-8efac29b2ac7','ecb12058-5002-4be0-925a-7a6a7d6369ee','f080a243-79f8-42d6-82e0-a17749840691','913b0873-6f11-42e7-bf4f-a5eabc8586a0','8e404327-4184-44fc-a2e9-12205211ebbf','3cb7d83d-6565-4dc1-817f-3bc01bb021d8','6782a358-b185-484f-999e-af58665e2aa0','2b721ee9-3c02-4441-8560-efbf1222c928','013f7902-79e5-47a8-b227-5632302071f7','d53c979b-a7b2-4b90-8bd7-ce20647ff72a','76dbd4de-0613-481e-a3b6-2ac51c0ad3d2','17e84e14-ea2b-4469-ad57-e8bbc854904e','389c1b27-a1af-4c0c-a155-b82f7a42b797','2a511cf3-7121-401b-9218-3534e3d63091','59757411-a8c9-4ad3-bd4a-1e95f5ad9c08','3234755b-95a1-41ed-8aee-c1adc2c4bc03','b7b68687-bc8c-459b-b4da-1bbae39dbc73','2c3c6da5-7d69-4cb5-a657-8c3d981cbf31','33ef06b7-a150-436a-9987-44c525939659','4866061a-00a9-40da-8909-94ca8c6beb9f','ae20d58e-6d28-4f88-9296-f1be29a570f7','f273efeb-8710-4a37-8510-1decd38403d9','f74d11d9-f50f-41c8-ba5a-330bb4fd3852','9cb9f33f-c5a9-45bb-8d86-a9a6e8f52011','b75454ff-e763-4af8-bbcc-326af8495d85','c7ebd74c-b9b8-4283-91cc-2794b3073989','b9336224-1753-4c17-bbc3-093d5decb4a5','3ebf9e55-38f9-4cc5-bd24-cd56ef4c4fc1','2d691cb2-7c6d-492a-8bc9-401a8e77d6f3','79fde198-b8d4-429b-9116-b7ed1589b1be','ded37c4c-2396-409e-803d-d10b13cf217a','d04c67ee-4cc1-45e0-bf58-8fe65e10d4fc','41a91ab1-7cf3-4ffe-b605-d1e66164d97f','725c096a-d0b7-48de-8887-7c0d90128a2f','6f2886dc-dfd1-4cb3-99fe-d85fe4ab8317','693ec7b5-9c20-43e1-9672-d9565b917afb','c7d0b865-541a-479b-8de0-586ecb093d75','378c6fbf-d161-4805-9ccc-b7545c7abaa6','15bfa21a-49e3-4993-862c-c31e2b68959d','fec12a4a-9b82-45c3-b2a3-1c1f2661db60','9ac5387e-5194-4cea-95da-7ad50867f11d','c88691fc-6f5a-40bb-a6f4-ac5652f40b97','73f903c3-291d-4067-b1fb-14b4df6618e7','9ddfafe2-64e5-4f45-8ba3-cfdfeed42c22','85478fd4-7f78-4ade-8caf-b6b5b2ae54b5','0b10bac3-3b84-46f6-9fe3-3e7b96e5abba',
'9d8043ef-9e79-4b61-b068-4b0f59773e65','11ccc357-91bd-416b-80ca-5ff7b2e3605e','45da868f-85df-44b0-985d-89367f44f9fa','c7f6ca71-b0ec-44f4-9f8b-50819515c150','9f455642-987b-4e15-9f42-3740ea1f2568','e58c5b96-25bc-469c-a041-e81db029055d','3529870c-bb45-458d-861f-b3430451f4b6','01bc4e41-d278-4b4c-bbd9-1d3667dbd63b','19099744-2de8-4a13-a5b6-b6e10ba0ddb5','66cff952-1848-44d1-856c-f011e048bf0a','ef3dc0b6-cb4b-4fbd-9902-95ddb87517e7','1ab85664-c909-4a9f-8b95-74ea97b8870e','0829d761-e10a-4cf2-920c-b3800e1a0182','8fe71ed5-652e-48b2-97b4-27f5bce19b4c','448f4350-1885-4176-a859-c794253bcd09','2360ca48-eadf-49c9-b884-0478546f24f7','2033f8df-ab2a-4e8f-9791-c3ceedba6aaf','1980fe9d-545a-4e84-8b04-75275c8a6200','78d3b70b-84be-4ebb-8dbb-e2fefa405cd9','31a897f8-0f70-488a-a622-3f110e98928d','3c5dd4ea-5eab-4121-b2f0-87b5ac190fd3','9b2d82cf-b0ca-4785-819f-99d60685964d','1ebff2ea-b751-4e31-8474-3eb86e4b848d','a414a53a-eed2-4d19-84ec-fbc6c9b8cde7','713de9b1-8ad3-4817-9d1e-e9b06cfe8eef','48ee19bf-ba01-4c80-a3dc-a497050fbb57','000072e7-d759-4207-9b39-5f78ca18cb38','3705ddee-3ed8-4775-9d45-db3d21358127','79d540f5-d26c-491c-ab15-e9ff7e482cf3','6802a6bc-b5d5-4e8e-9b25-945752da97a0','2a7068ad-a54a-43ea-8cf4-56df5baf540f','2a6ed5cd-1d3d-4ff1-8e47-519673109bc2','4d74cfef-8ba9-433c-9019-71a5ee859251','d695deca-1608-45e9-9165-ee52ea5ac89f','885ea92f-1dc5-4e3b-bf4a-1a4254266251','a5f7618b-f745-4cce-817a-fe7a709d89ea','a66fb06c-e6ca-428b-9040-71a5de25ec5b','c1924607-6333-423c-8aa5-f3c6b555476a','5c06db76-b174-44b3-8e2e-df7b20ac789c','496e5092-c28a-406b-9a7e-212bcefeeb27','d51c7893-3666-488e-8657-a6d277443d0f','ded4ade6-bb42-4e93-ac0e-ecea08e6c640','1fac3348-2fe5-4e6e-951d-da1c7897dc20','d1dd24ce-e058-4f1c-900d-667e39dc4547','d10c2461-ab6f-4cfd-8f34-f0fbdb549ef8','f934ecac-b9d1-47b5-8183-4c22a8d9d3a5','ab526539-18ad-4085-b8b4-68a440ab1d94','93e07c44-123e-4f7c-b618-5a9c428a619f','37857d1b-7f59-4695-ad48-c0371bcbaf0b','8a9611d1-bfb7-4895-8b34-a070be9118ce','b7ff8aa0-fda1-4ded-afc8-cb9df67fcff6','98088837-f15f-46fb-aa0f-4484d2ed3069','f2243f38-8f29-4c9b-8e1e-33719f0de3b4','4f1e7c7a-f2e7-4fec-a193-51fc06397096','2a9c520f-bc2d-45b9-8c5c-8f54feddf5e5','4492d2fb-e6b7-447b-bcb7-fca2b852ec20','da67c54c-6eae-447f-8415-03c19c69c078','d8dc6d72-1443-44f7-8604-3466a56d8900','ba56e1d0-e9eb-49a5-bba8-be2e155b7f86','978a9443-006a-4f82-9d5c-762ce82b6560','a8d40309-b138-4799-8508-91c40a58a849','c320e986-dc2a-4c9c-9009-3ddb6ce7b337','af58ef81-4026-4e3a-9002-fdc1055980da',
'eae61cde-345e-43b7-bdfd-4d7ee670125b','e6c4e4bd-d715-4ee1-9b4a-a601aca8ea99','4ae8892d-370c-48bd-afb4-12457592694b','d4a2fabf-c98c-4e8c-8b42-b53f4bcc8a23','8942af4b-df94-4fea-8d66-8125a6762e4f','0c551519-29cd-4ad9-828e-264b79d99d5c','7c71bf39-a00d-471b-af02-7f27715c8446','2655c39d-2f4e-4c6d-93e1-00c7b8babf4f','44fe3e6a-7301-4d8b-95d8-fc50267a164a','0a8eb2a5-7747-420e-a0f6-97117ab94ef6','61bb999d-7a5b-4d57-8ccb-424ffe7e5f9d','8f637151-53c7-4e69-91b4-82e4ff991bee','5dc398f8-7119-4eb1-947d-a1edad4646f2','19e43a12-9abc-4485-bf9c-5ff40c1191db','21eaadb0-c123-4bf4-acbc-486b722c2533','175b25c7-f931-49c9-a60c-372f8af767ca','1239106f-8880-4b00-b863-4c7e7e1df544','c58063af-f449-4b65-b906-5cb6dcd9367c','a0766187-d40a-4bb1-b483-bd00179e9204','f8dedfc1-a4c2-44c8-a244-f6da0d2824ab','47df7aec-46a7-46bf-a289-dfc219ef0e55','1b425183-89d2-4641-94ea-8249c465414f','8ec612c1-da23-4de8-a752-d879280d81d0','baa9c7f5-d6f7-4800-abb9-5e0bef2b2130','9590189d-412e-4af5-9d86-f3a11e8c6d86','fe57b6a2-a7f0-4061-a0a2-9b40938405d5','8208c2b3-4430-4711-914c-6b4e014be655','882d1c2a-de2a-4fc0-879f-e00185ba260c','7bbb9ad7-59e7-45ed-837c-084006192b40','db159c34-ce7d-4abe-aafb-a6f56893b042','3efb2ab9-2231-4125-9e5f-a58e76e08923','8e434e31-4cd4-47bf-8861-8856bbb77fd9','77a38eab-97a1-49b7-941a-79fbdaee0736','14225086-7b05-4ec4-a155-366046d0c294','6425fd5f-1aee-4307-a17e-15fd1262674d','6089ad93-30bc-48e9-be7c-3033820ba468','158b588e-9e6c-4d60-b823-2b17c683133c','3163471a-8117-4dec-8dcb-5cd3ba66a897','85be01a2-d97d-44aa-b682-e52581a8d731','8330921b-4bef-4bae-a201-2742a4ab4b41','85a8f6da-2c65-41f5-bbcd-03633e238d75','5b8abdcf-40cc-49ba-9372-71344a21200e','6ec03c48-5cab-4fda-9ac7-0a335b11c573','b63671f6-a987-46cb-839f-670e55019c89','9fe1944f-2721-494d-9425-864efe315c31','c19b1632-75c2-4513-99b9-2b6bacac406e','1e802e9e-29c9-4dea-9973-461d5d9c0d3c','9939dddd-65c6-460d-82f2-afdea6c6182a','7c2433af-d8ae-4259-99e1-30d009c05fdd','09996ce2-a16c-46c2-8efb-84dd82ebe853','cb016592-855d-4d61-8941-4ca6389b0e07','4fdbc4f2-a708-4171-ad9f-02a9f91432a9','10512825-cc2f-42e5-ac46-165a6ab8bdd6','35f59c13-80ae-42c2-997a-0c5bf776f3b9','bdb47f49-467e-4e5f-be41-0b664f9917fd','504d8bfb-35d9-4c7c-8d8b-2c2459b9a275','036cdf0a-e842-4dd2-bbf2-85f050e3371e','fcb86ad6-2b08-403e-ac6f-2a4ef701b0f5','80f2c0d8-494b-4ae8-8617-13220616fee3','1fd3342d-8849-4fc0-b239-00b328afa4d8','fc803003-0cd3-4c16-8616-0f60e9a8b692','3d22ddc0-9ae0-4d55-a081-2b853839c64b','d3b52f10-afa7-4bde-a129-508f331ab840',
'65706ecd-8108-41cf-bb2f-d77ace35d7e6','26102daf-394c-4a00-bd03-dad7c7ab4670','5737a913-adde-4abc-b3cc-b4e7bb2b5d61','aefccef8-3f46-4078-a2d4-2e6821f8c3b4','12464ecb-7028-4f77-badc-399524aee674','8afff27e-f080-4a41-87f2-dd290a9a02a1','cb1a0c5c-5ba3-4cfb-a606-a700d8f06658','0de13e56-7ec5-4975-9563-a99dbe9af1d2','3a8f0a93-fdf7-4dea-84f7-df812238bbdc','703944b5-a835-41cb-aeda-46edcd547d26','71ba88ed-46e0-4f31-9e2f-da247ad8876a','70a9a163-fa98-48cf-bb0d-10c6d84fb7c6','fa66dcd3-8398-43e7-9d73-a186edb3959e','d79a4ada-7399-4fe8-9e45-22d16d0f8ea1','027d9783-5e7c-4cd8-b3bd-6e20d96f5d5d','d9a85bcc-fdd5-430b-9879-cf03f5fb5b9f','eab633e9-e31f-436d-95bc-8ce963fc1594','553eaee8-e856-43fd-b938-d371f492fd48','0b166688-57d0-44b5-95d6-dcd95f3d271e','d9b56d7e-2904-43ca-b452-b536963e14bd','ee1e6dcb-2c4d-4074-a91c-64aa19acdfd8','1f5031cd-65b5-4014-85ad-df171cc2a6d5','45f5ed75-77ba-4d75-b822-6bf697f03732','ba813822-13f2-47f1-b197-d1b50558fc4f','a5f03905-5477-4843-836f-95c985faf771','5d88b981-0a24-4974-82e1-0e16676607b2','09b09e09-82bf-4713-b0bf-0a611e654bae','86419a4f-bf13-4467-8516-bf78a9c8f40a','a6042572-6003-4e1c-8af7-18ddda1427e1','7cf54cce-8a6c-471b-bafc-b627ef166c51','3bd1e655-6dee-4a93-b9fb-dcfac605b963','0b275c5c-3efd-45d2-9db5-134226a79b23','7e9f8a12-803f-4bb3-b034-b399b87cd234','10ede603-1514-4165-8427-83326b9267b9','d20e523e-c14c-47bc-9650-a226cd751feb','fc614e4a-7d86-4c39-8552-711db7e80033','7f10f35c-bdf2-4ec3-80e4-22c6e7addf4d','ebf1431c-5178-415c-a4f0-a14c211f9836','be747404-88ce-4747-9743-16be4ceb0c2c','98972826-9d1a-471c-af70-d4092eddabcd','ec1869b5-260c-43ef-a30e-5a412f594085','bdfd402b-8fba-464e-b6a1-22c487fc73c5','e91331b6-e78d-4fe9-b1ac-614d8aa17744','55a1c81f-4dc0-4689-bf37-b9e36429be93','cd222e5b-b9af-4f09-9ca1-46f0670657a3','312f55d2-6b83-47d9-a611-0010cb8d61f3','e498ebac-8b57-48ab-b3e2-07708598c2ab','601a8e36-8949-4dcf-b52b-aa223b0136b3','a821dba1-d981-4b60-ae6f-c43fcb1340ba','5552401d-7b94-48cf-b5cb-f79a1558ec08','1eaa8dae-b302-49d1-a8e5-c46cb8dd1d6f','d408727d-066e-4ca6-886b-a6a7bb4c2c91','b226a1ca-0594-4f19-bd72-73e586df1b17','aace733f-3dac-4d3a-930d-76b52a33c525','8db34bb4-9931-4322-96a8-ac10e5ab490f','a49628dc-34b2-4438-95b4-5d8c5acbb16e','f1fe2607-9937-464e-84fc-f123a27a241c','f7914d45-e372-4c6b-b737-291cb808e065','ee7e2868-0dd8-4338-b685-4c76373ef705','ec3c1696-38fe-4426-bdf3-7b568a6acc1a','5b7b0fb7-dbb5-49e1-8876-82938f75a35b','60076109-262b-425a-b260-74eed357919f','16e90950-68f9-48c6-b629-3d638ea8e0b6',
'930a48ec-0668-4ea8-9e30-b661e96aadcb','6a7aad15-ea7e-4dad-a409-3094f9c89eb2','b636b26f-05a0-4a8a-83f9-e41061172165','6d5ec461-68dc-4e17-a275-2f0ff0954f35','81791441-4391-4d82-aefd-779b0a099b89','f3c0be2d-7e57-4a0b-bbac-85386ffc81ea','c984e621-a8e7-41da-a8f6-010ad5d89172','8f81e61b-0c70-4f8a-ab79-4125e31d152d','8535a75d-6f11-42b4-a9b7-6631ab85c802','4b941031-2401-459f-92d8-79e9e115f58b','092d0911-b079-4253-a5b6-84741da782f9','48895356-5a07-4d24-aca7-e9afcd0cf2e2','b4b0d55a-428e-49dd-bc7f-972ad922e7be','960b244a-d1d5-4e59-a745-81399b0fd868','1fe307f5-b44c-4975-976b-f6949739b647','e73a428d-e81b-404b-80cc-b7a3a79d7e9d','718503d2-f91f-48db-9079-9475d1059817','abfa0cbd-7eca-437d-9376-9dad2bcdb45b','03a3f8e3-84be-422c-9109-c31af2345410','12f114f4-380f-40fc-8fef-d96d4ca5e5d5','b58c1884-a18b-4629-b710-a235b871b133','1104d18a-d7b2-40ff-bb11-8e0de64fd671','be263be7-31cd-47f4-8399-f856235140a7','c1c30793-2011-4ab0-90a6-0c8fbff5aceb','5baaed91-049b-4dde-8dfc-4fa1597bca99','7cf7ee84-7710-4985-b645-5dbeb2d314d5','e5856555-b09b-471e-aa17-e5aa14a1fe72','94397b57-83c4-4c2d-8519-dfc5d58e684c','eab899d0-5675-4955-b4c8-156727516580','af931b0b-2783-4c06-bcec-22fb35803ee2','bfd6360b-50db-40c7-bf6e-f2e1b22de4bb','effa4328-5199-488b-92a7-3e3663b3ebd1','f791b62d-9e98-4be1-a753-aa075de05ea1','96e37634-7a32-432c-b15e-bce7ae5a0b23','1f1787f1-4059-44a9-b391-01053c347c6c','92497a89-873a-401b-88ee-843540237359','66b26660-5c70-4e1b-882e-8a0c7a488aa6','eb6419cf-3e6f-4a35-93a8-abf86ebbf8ab','e961c465-0af9-48ba-a0c5-083db9a48565','b6e9b864-0214-4358-8ea3-7e9422b879b9','452f10ea-8823-46e6-98c8-228cc11d0ca1','18f3a294-2ded-4801-9a59-c2b339342fc0','03c04bba-6595-48b7-853f-52e74327b049','4d387fae-711a-4116-8c22-3721b2c14611','291acbfa-5371-4639-9f04-8b7ebc641877','fc0216d8-07e5-46e3-b942-e7fad327f6bd','363b8111-69f6-48c6-a732-bfa140590fef','8fc5fec1-bd21-4fa0-a487-fb371c3307d2','31ec1a89-b990-40bd-a524-7aaf4e988d4c','4389bc24-82c0-4817-8e37-474bb3abad0e','a280f7b3-6568-4a2b-a516-249f752b629f','8675f1a4-8e0c-4442-8b0e-c905d1e2d9dd','e4fdb61d-5fe7-486b-8a7a-98def9ffe69d','55082ece-af58-4afb-95e3-9e293228b69c','dbff19f6-0499-4095-b042-7bd85d9a2bf8','e1452d58-dd95-48f4-85c6-c769da2aad46','8551e9d7-410e-4b07-832d-e7b60bfab082','19127c25-2bc4-4a6e-b466-541d075cad1b','1ecc28fd-4e28-498d-8031-a2a40119e7ec','6e7e33e9-f952-429f-9817-f11cbbcb8eef','d48f3a2c-9dbb-47f3-9a58-e6c449532893','adb41f87-c779-4116-8079-25eb562b3e3a','3f2ee561-97e0-4fc7-8444-4033dc7e018d',
'e0c920d1-4a20-4eed-b5b5-1b1a793ce902','adba1c89-9a76-4c1e-9550-26aa332b88e8','d5d8ff23-9602-400b-be92-26a8121f7b6f','ee2f5c61-6b5d-4c94-8791-fd32a9da76c3','619d2e92-7ac0-450d-b820-b56675524330','5574e3cd-53bf-4b6b-966a-561ea4e9057c','553d4295-39e5-465f-9aed-5c55b8d4c554','d1054bd8-a2fb-4f8c-b9c3-20c8fd8faffd','8dc249e7-c9c9-4500-8ba6-2f557aa0121a','b0918071-1dc9-43c4-9ae2-cbde9469fea0','734feabd-9355-4eae-9320-5b95334825bc','20926544-3f4d-4891-9afd-cacf9108380c','4067db3c-f2ea-4392-bf19-b04bb4c2ca4d','0107a5fb-72c5-48ed-a9c7-7cbc9074c4eb','6a3114ce-76f1-485d-848d-2f9abb9ef8b7','4d5220bf-3354-45ba-bbfd-1e739df7353e','b54d7d3c-5221-42b0-b053-f152b71f009b','050489c9-a1c9-4e24-a097-81725806a399','872ac525-46ad-4a81-bdba-37b1ad9a060a','2d12312c-2e82-41cc-8ac4-94d6905bed8d','ece5d3a8-ee65-460f-b482-9e8b1edf1f6c','bc0bce73-f5cf-48e5-b61a-d39f2c96c913','059f10bb-e96d-4b54-b3f6-aa0aa881bcfc','3ec8026b-cf06-4b58-b8bc-962b8b419fbb','5f4ccad9-bfff-4c82-bbf8-b0372a31493f','45769c7f-5b6a-47fa-87fc-bdea15a99cab','da240cba-f347-4912-ab82-a641f5ad094d','30e6b84d-b097-4b44-bd8e-d92373cee27c','bb04ec3d-2d4d-4f84-a128-d8e2fb456ebe','3e8a4764-a1bf-4cec-b20a-de6ec0187345','32f9488e-0671-4cef-a303-ac03e9dcee48','17e0ae60-1d6f-49dd-8862-8fadffa8d1bc','aad0895b-8d68-443b-bb57-2648e726bbcd','82501697-6e33-47a7-9cfe-51827afe0e58','9c8dab63-ff5c-4194-a825-55c6d9e49336','d14ad0f7-7a04-4e29-a25f-dc7b5edb9a62','f9fb4704-6b98-4bf8-bcfd-fa2657772376','6db6f71e-f205-4961-9aaa-532335b0273c','b613a58b-2fe7-4b7f-ad5b-225153f85c47','46d12352-ebc0-4a43-92b2-02df31ec408e','b112c27d-8933-4d0f-accd-ca28d2f71fac','a022fddd-98b5-41bf-9494-105bdff6e700','a803b7da-db27-4663-a7d8-5ed67f06990a','b783c470-190b-4a59-9420-0425fe93eab2','217ceeda-a77a-4c57-9733-df078f4ae150','5de0e5a1-b60f-4713-8ab0-e50b0ad4f82c','b8edb870-961b-4d61-87f0-eb781cf86777','29fa35f8-4268-41e1-8e4d-d651bb4bf197','abc0eb5b-b090-4b54-94c3-1f1badcc405c','5980c8db-23a5-4d98-9407-b0354f05d97f','175c90a9-a10d-43a8-86a7-761df5a8f493','f378cdd0-f124-4b65-9c51-8a02988436fa','3f1f15b2-2f4e-4e59-b0a9-6962802f0be6','3debdf97-a82e-4285-b454-3ccbb49929f8','b34c7f6a-4a46-48ae-a110-76e4ba273bca','a5cc2484-6d7c-4366-81b6-f4592138a83a','e96b3810-1e3d-4ada-9374-f41a67d4d584','3e45bb21-2e4a-45e1-8c82-4f19ebb47ab4','1059b57f-b977-47d9-a913-ebb24d21ce76','36335bc4-2dae-4839-b887-e91ea7250d4f','17a6ab38-385d-41a6-8110-10c339f8e3e8','4e32621e-f634-40ac-9cfe-e23d43f805c5','85282300-a417-4f58-8dfa-a7aee72ef8d4',
'18a1266b-b5c6-40a0-a026-8316d8fa392c','a5d0433b-e4e7-4b70-aea6-dea1bfee7626','7eea6107-9137-4c77-97f3-b9ef32b53fb4','fafa7aca-6452-45c7-95ae-2ca825720c61','6ca7c3eb-a1c3-4c86-b3dc-c64fd248f1ed','25d1be15-67f2-4c60-94dc-e18a7a78d8a2','4b4490f4-1228-4d2e-82a9-eba2deeb63cb','6b037222-4328-4815-8943-1af3f206fd99','8aebb272-ddcb-4c57-91b1-83a3e9c0e0d7','c09ed455-8997-4f28-8749-46cea3f95745','58bf53e2-83e6-4cff-a7c2-818319db4a41','3e162f96-279f-472c-9bee-089ec67e61a2','8150f81c-d6c8-4482-b3ec-c65e0ed39ee4','f58a4b6b-7819-4377-a524-aae7c81b02dc','229302de-5730-47c4-8860-136c151d98aa','a725e19c-60ab-4f67-a800-1a563904c5db','c9587513-75d8-469e-8d1f-707969a6dc8b','adbe548f-97df-4650-9ecd-4c0d0b462319','4031c4b2-559e-45da-a437-f9397589a7a9','5fc9fd49-c1a8-47a7-a7d1-3c3a980241fa','82b061bf-60a2-435a-946f-76822a9e3251','68b90cc9-5248-4218-8387-c67b36d554dc','3e1e01c6-2849-4113-ba94-73cb626bd8f1','ecccb39d-f476-4448-98c6-26df9a85daab','9f503741-bfa2-4dd2-8251-e3f40979d5e5','859acc11-381d-4969-861f-4abd42135051','3126cf72-c9c0-477f-9038-a5466753a8a0','b8b6194a-020c-4e37-aa54-3021a53d2b5e','f25d0978-1a9c-4d52-83aa-5e3d5f39e099','aa367217-cfc6-448e-936d-0a65af2a9ddc','e800e352-9938-498b-b63a-782e4ae59016','e7301e4e-062a-4fca-9581-bb51d228fbd0','810fa3fd-f5d3-4a5d-b291-66f25897372a','49d891ee-f71e-40c6-9355-96e1d3ecdfe4','ffbf65bd-3cd1-4f41-a8ea-2a1d2743b88e','715cc3d1-4fe1-406b-9cb7-a482084ac315','39080007-c282-446a-aae1-8a0b4c9ca1e8','d5563e9e-d291-4dac-8e25-477363a394e6','891a881f-e888-492d-9c0e-662b2db8b0de','0a49488d-df88-4950-a4fa-b90a394c04c4','efaa796d-b220-419d-a0ae-7d4cb34908b4','c8cf9dda-33d6-4eea-9c4b-152d0ef6a062','c5d31678-a494-4861-b61c-45c684c42df3','33a978d8-25cc-4578-a813-dfd9982a6c5c','729747d9-db74-4730-9737-25c5e8de3f2c','8fc552ab-da24-4722-8785-dfcaca0a2ed7','99100cbf-d09b-47dd-ae20-f1ec5a4106e5','86aed510-9006-4078-b94b-8bd0422cef2f','efad10f5-1f0d-4cd3-a106-ac1d9db9bc30','00861e2e-7d1f-4414-850b-c8f27a9f2177','d664e89f-dfdf-46b6-9cf8-7ae08a272a97','a07a16d1-9381-4066-957a-e88aec486368','7c997aac-2926-4f1a-a436-151beed72719','65d4af4b-edde-4cd7-af80-35ca63184874','14c78d92-e06a-4801-bd7c-5f70ac00a509','f0efa84f-8c60-427a-ae6e-8e6e4781df54','98003577-702b-4e36-9285-127608b87f09','fa2db430-44cf-426e-be81-28bbd75b7862','601cbd5a-a23c-4d6b-8cd1-e42b1d50addb','dd9e01fb-6976-45b4-bcee-5ecf3fcdbad0','d4b90b7a-0800-4743-95c5-6e972111b9f3','2393a21c-9666-4eba-bfc5-c2a93860418d','d0a9b3b9-2644-4342-ace8-26cefe38b8e6',
'fd3a3292-7ddf-4a9e-afc9-cc88d8b4a4cb','2e3807d8-8b13-4b4b-a9eb-7bdaa15998a8','e2f519ee-1924-48dd-aeef-174f854ba782','74f14e36-8cf4-462f-988e-4590056da183','8a8c5d3b-20c7-46c3-8333-e4be7a035735','b48c0cc0-48fc-4f6d-aefe-87cb4f00eb78','04c52b31-ece2-4cce-9cd6-3700ee80c480','fda66547-89cc-4a5f-bf39-f8987022e5bd','1e3e83d8-3e06-4c6e-ac4c-7e3dc543be0f','aeb97ebb-6b0b-43b2-8882-818b136be4f2','98f358b4-804f-42c1-b006-8953af10c610','351da875-8aec-444f-a724-f5c33678fc15','abb8551e-1114-4b69-b2d5-8a4eb4def526','b3067095-acca-4532-8157-615b760c31e0','3350e873-2ad2-4985-9bb9-95e8f83d8418','02372f1f-3d9b-4f9b-88c5-b6107265d301','82a6020e-bcaf-485d-af92-5b315fc0fcc0','ac5b7e90-4f6b-4cd3-8cf5-23e20c51c9d2','d7f94e33-6615-4dcc-9e80-8bc56cb11b9e','45670eaa-0ee3-40cc-bcd5-498949bdcfa5','16fe55cf-673f-4b64-a5c2-4d2d9e68e45a','0c1abd68-8c23-4a48-8ccb-8d87ac67e57d','8a6504e6-ace1-4488-aba1-363e043a7e94','28180c5a-d92b-4c9c-89a7-9816b887290c','1f0aa4c3-f1f1-40d6-b552-9003e77739b5','7bc3df88-ea17-428b-ad66-1b44606fe635','31e2cda6-c36a-49d4-9c77-0d8a74f49d0e','bf9ce53b-e64d-4bc2-bb6c-7cf29e095694','4426aa9b-f7fb-47ee-8163-885fe83f1456','3c5bdd13-c601-4334-8ce6-3576a8b40b5e','4ea88c13-5cf0-409e-8e3f-08b9c595adbd','5782c566-a733-41d2-8aa7-a0b2e18f549e','15029790-5203-4f8c-ae70-7d6684ecf138','3ba63e05-4320-4068-b86a-aa92830b1e7c','eaa67850-4d24-486a-9d36-1fdfdeec0b6c','fd223f4f-b3d9-4f4e-a3b8-195bf7e6c034','22a3ce0f-7aef-4e2f-b4cc-85bb761d31a9','271a305c-6351-410d-9912-88318f5c1be5','8377174f-0788-4028-9d38-956fdb88a4b4','cb434fcd-4c80-4cd6-b7ba-f2273be3b15d','cc715637-812c-4d8a-9c31-ef1cb8c7999c','0d17cc08-3986-4c1b-8224-9d911178d842','029b18e4-de3b-4796-868f-76b9e21fb688','9505afd8-a510-4945-8d22-dca4ccca71f2','f2366360-544a-4f47-96a4-ab3af24de4d6','da66297d-ce53-47ff-b339-6e723fa21c75','f3d00b10-3dd9-4b62-8240-7ca998e9b71c','2d82eb07-3f1c-415a-aaf6-69fa69fff20e','3b9744a3-7c23-4c58-b51f-b72f5eb61d42','2ed46fea-8e5c-443c-b0a8-ed9176a2c715','a572bd6a-e2e3-4960-a0e6-3c7efa413755','4ba4591b-614a-4863-a7bd-664c69913217','6b6703d1-fc72-4025-b9b8-b4de49bd0eab','23469681-0189-4f58-b457-4a7bd803e23e','67b7d660-fae3-4df9-8207-c4cecfe2a220','4d4b55ad-4bfe-4987-917c-d13cc3555bcd','6f4cf9e8-5375-4901-9287-3f75ad1a8ca3','f0b49f22-335e-463f-91a9-0dcf68fc94c8','52afdd39-eca3-4766-97fa-89172a21d750','5967bb5c-b8f1-41b5-bc7c-535e6e9aaebd','673c2fb5-8873-477e-99e9-de8aab5d876b','d62c1198-cb85-427a-bd8a-a657f904ed4c','c32bfb67-63ee-454c-a9ce-14b619136f47',
'400147d5-8a35-4ba7-824d-664b761a2baa','e00f98ba-339d-443b-bab5-81a98b02840b','68bca82d-2004-4b8e-b073-9bee223102c6','a4916940-ac89-46c7-93d2-eb8ccff3f0d2','e598e9d9-d4e8-4c36-b45e-973a8befad9d','1c4eb7b7-666f-4517-b79f-467197966260','c1abb88e-78f4-4820-8a9e-9546acf5a458','737bf744-2b48-449f-855d-f3bf6c1e65c5','06430149-6416-43b6-b12c-dab6b91aaaae','b7acff74-32f8-48e6-9c61-7d9cdb596d42','0e0b1dc9-37a9-482d-85d9-d58ec3bf11d4','5770d4b5-87b5-4c08-9775-d43e819c3072','2b70d1fc-e992-4969-bc8a-c0ca28a6a947','a792b759-baea-4a87-90cf-89c7b05fa8b6','3a2f6858-b6ac-45c1-a360-bce60a92caaf','2a26f48f-12b2-4aeb-8ff8-c74d94b93272','4b0cd9ca-c602-426e-a93f-a21211e4d557','839b4b97-ba92-43d9-8a69-1897e8b0b195','6ecd87ef-9231-4b6b-8a78-8ba2c3cd4927','6c38addc-83fe-4747-a0a0-a2c97eac7154','8fc39e96-9e0d-4213-9e9a-bdc393d08c7e','b7146a11-9d54-4c9a-baf2-87c4a7723f65','87beada6-f813-4a54-bead-104908808bfa','3cba8459-8677-4cfd-ad00-be701a520086','b5700b26-b0a6-4677-9c9e-1aa0273d33e7','fef81d1a-9566-4910-a327-ec233add4417','7310af63-1e40-4eeb-8b75-a44d7abbbe26','1cbae5ac-9125-4ce7-928f-1ca60085384c','e0d75393-e4d2-4f6e-9bd4-33696340fc8c','918aeba4-3503-4c05-8cad-57f9eef891ae','350fa155-7201-40b8-b533-cc8c75273362','7a3a6bae-f75f-4cfc-a8ea-41133c12b2f6','ed7f98fa-2d23-44db-8dc4-b0af05f351b1','5cdf1a3e-9b4e-4a2c-9bf1-cb9b090fb03e','490fe374-bcea-4c2b-bc08-21552da2892c','0990f77a-b86b-4def-95ff-3a76a5f8c486','59a4ab21-b81f-4a2a-b862-bc732b1963c3','586a204f-271c-4e55-8024-19e4e1931f52','bedc087c-f3e9-4f52-976d-5d048a71a0b9','2eb3eaed-d87a-4007-82ca-dc85677f3139','d77540f1-9d60-4b0e-a527-932860e3e4d3','8cd62fac-12f7-4725-b1f6-52fa2a56a43c','2abafd89-9b9e-4d35-8a37-813f278c87f0','dcacca56-190f-436f-9b6f-504795c551e2','24d3fad6-dadb-4dc0-9158-dac687c3db8c','7a5abd23-aca7-4869-88b4-dd2b114c087e','7d74fdff-aef9-469c-ab06-aa6aa8ee7664','874771ef-b1f3-4233-b0c1-0865b068fa1f','0f006892-1c68-4c52-8c76-a3c97bbca7fa','1bf52b7b-f988-4d2e-bebd-739278e2b5e3','fed5680c-3679-4db6-98e1-be4753a956a7','bd1d4ad6-2e9e-4d4e-803d-8ff811e353d0','50fc3c53-7f1c-4be3-84af-c316e5f08c49','82a8b707-11ad-4aba-87ca-e1530a69ba8f','8d450d0a-d793-4ad3-8a92-9d67d2140fe5','06bd8245-ae1c-4faf-b136-9de4a0f95190','02c13bb6-615b-4812-8d46-ac1420f06ab4','5e30e141-0426-4c06-8042-d799613cef77','ab0581d6-ce2b-4f68-a660-b478ed6ce103','4efd25d9-799a-485b-9240-463fe42832fe','33942006-7afd-4fa7-ada4-9a9706d118aa','18f9c13b-04ac-422d-987d-23d79facd7ec','76722634-c4d9-4de6-8e3c-d144ae5072a2',
'c3c38487-90df-4254-b31f-a155b3390242','f865cd59-7941-4447-96ef-9664cdee454a','95b5c899-a8d1-463f-8d3f-b983f7dc5990','2aad053f-2009-458b-9be2-400aacb356f1','fec5ab24-da83-437e-a442-2c9dc1670663','064826a9-f7b0-43cc-9c20-09e23a4f2a8d','7f2d4462-9d15-427f-853d-d8d5ed31fa5b','db5866cd-db6c-4628-86b8-c4c015ae0494','c2979281-7e6b-46b8-84df-7de449973a6d','69ef9a0e-09a8-48e8-a89f-0de311550e8e','e10c2be6-9b73-4bf0-80c0-eae7f2a076c3','ee8a635b-3561-4543-aead-bf2bebfc00e5','cac95e1e-3486-4524-8464-dc6477d62052','20f8f0b4-1cce-4786-84c4-aa7acbf38aaf','3b489fb5-54a4-410f-84ad-ecd95eddc932','84b0770d-c467-4080-a7c7-3cc022199746','9343157d-0486-4333-ae98-b775c880fcc6','d74cdde9-3797-446b-ba40-c445b72caaab','ef39d0b9-7944-4912-840b-7407f94664c1','acdc751a-b40e-40d8-ab01-952084ee7791','f8cbca2e-6e49-4718-bf64-f2bc0cc7469b','6fff1fa4-a8f8-419c-8d5c-9f0d38a99da6','d4302b05-4ba0-4630-9105-fb1d91b7fc81','a9f50f66-3beb-41f5-bceb-d20086e77489','d33b55da-843f-4946-add1-cb19dcf7bae2','ae9635f7-08e7-46b8-a70a-4e722f5d0970','41a75bbe-b9e5-4390-a8ec-5339dea41355','56dc5eec-fe65-4df1-858a-deee668aab5e','8f2637de-2894-4930-9278-c910c029fa3e','5d03691b-1f91-4df7-b07c-a8237a9914b4','9c079474-d340-4fd0-a2a5-3a71e86aba25','d321094d-3763-44fa-9f1e-ffb42ec6b73a','bd861eb6-64ba-4d3e-a36d-71a1a2320cba','8644061f-7695-4f83-b770-b891f24b6f98','f0a542a5-eb07-4faa-914b-fe6a094784fe','83a24c83-c178-4ad9-95ff-59de7bef358b','6804b29b-0f25-4991-92ac-252aacc150dd','083915d3-f5ee-4938-9bb1-3bdb47f5bbad','6bb7100d-29d5-480e-8e39-464a98395fbb','45dc23c7-3d57-4bfe-9ebd-41970ff1a74e','1f4f68b8-a5f7-4dcc-919f-79fe709d4017','5944d6df-8b2e-40a4-9eea-c9ebec10a6c7','f3086406-98a4-4707-b7fd-5550f7f310e8','c4067c6d-4d4d-4221-9c39-1165f7ce39b3','7b61fd1f-0064-4575-86fa-f311415cd836','e89e21ee-5cb3-460c-b9ed-4994f5531d97','a2e60397-163e-4970-a5bc-16a7219171c9','867c49cf-3cd7-4bee-b606-d98dac851a25','23abff5c-8142-45a4-ba20-d1354908fed7','158f890e-b771-4f3b-98c1-790448a0ea74','dfcae3f9-d00f-465d-a185-a92d2be90170','ccb52546-fcd8-40e9-af23-5e3c34a86574','9262025f-dffd-444c-a728-d8ce9a7a60cc','a2b420be-d28c-4e2a-9f52-7634c5e71922','793274c9-5c64-4dad-aa5a-119275c83f07','8917cb54-b2eb-421e-9191-f6487fb75da8','a6aaf894-7333-4582-a53b-9e61528d6cee','3bb0d83a-2b99-425e-bbf0-8a181d1a2306','ac2b4eb1-d35d-46e6-8e1e-cc0f8fcabbda','4b4c44df-8e54-4586-9d21-35c3a1ece12b','775d3221-212e-40b7-bb19-b9b727f81c03','b07148f3-4833-4e61-9a8c-4b3411a2716c','ef260cb1-383b-4872-9b17-898519d39b4a',
'ad1dda17-aed1-4b1a-84b5-14b418df42b0','40c31cc5-c977-47d8-a1f4-ad3305770a2b','778d9a4f-ebca-4a85-b263-0f2ad81ec869','9e20bc5e-2900-425d-bcc8-92558cf490ba');

SELECT  lpad(' ',level-1)||operation||' '||
       options||' '||object_name "Plan", cardinality "Rows", cost, bytes, cpu_cost, io_cost
  FROM PLAN_TABLE
CONNECT BY prior id = parent_id
        AND prior plan_id = plan_id
  START WITH id = 0        
 ORDER BY id;



exit;

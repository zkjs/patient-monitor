/* 2 floor drawables */

use hospital;

db.drawable.insert({title : "杂物间", type : "AMap.Polygon", data : { path : [ [ 0, 0 ], [ 144, 0 ], [ 144, 488 ], [ 0, 488 ] ] }, part : ObjectId("583690be42aa10288050037a") });
db.drawable.insert({title : "电梯间", type : "AMap.Polygon", data : { path : [ [ 144, 0 ], [ 360, 0 ], [ 360, 488 ], [ 144, 488 ] ] }, part : ObjectId("583690be42aa10288050037a") });
db.drawable.insert({title: '护士站', type: 'AMap.Polygon', data: { path: [[360,0],[720,0],[720,488],[360, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '值班室', type: 'AMap.Polygon', data: { path: [[720,0],[1080,0],[1080,488],[720, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '第一治疗室', type: 'AMap.Polygon', data: { path: [[1080,0],[1440,0],[1440,488],[1080, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '第二治疗室', type: 'AMap.Polygon', data: { path: [[1440,0],[1800,0],[1800,488],[1440, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '一病房', type: 'AMap.Polygon', data: { path: [[1800,0],[2160,0],[2160,488],[1800, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '二病房', type: 'AMap.Polygon', data: { path: [[2160,0],[2520,0],[2520,488],[2160, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '三病房', type: 'AMap.Polygon', data: { path: [[2520,0],[2880,0],[2880,488],[2520, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '胎监室', type: 'AMap.Polygon', data: { path: [[2880,0],[3240,0],[3240,488],[2880, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '产房', type: 'AMap.Polygon', data: { path: [[3240,0],[4680,0],[4680,1220],[3240, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '空', type: 'AMap.Polygon', data: { path: [[4680,0],[5040,0],[5040,488],[4680, 488]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '楼梯', type: 'AMap.Polygon', data: { path: [[0,732],[360,732],[360,1220],[0, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '医生办公室', type: 'AMap.Polygon', data: { path: [[360,732],[720,732],[720,1220],[360, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '抢救室', type: 'AMap.Polygon', data: { path: [[720,732],[1080,732],[1080,1220],[720, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '五病房', type: 'AMap.Polygon', data: { path: [[1080,732],[1440,732],[1440,1220],[1080, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '四病房', type: 'AMap.Polygon', data: { path: [[1440,732],[1800,732],[1800,1220],[1440, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '库房', type: 'AMap.Polygon', data: { path: [[1800,732],[2160,732],[2160,1220],[1800, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '女厕', type: 'AMap.Polygon', data: { path: [[2160,862],[2520,862],[2520,1220],[2160, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '男厕', type: 'AMap.Polygon', data: { path: [[2520,862],[2880,862],[2880,1220],[2520, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '新生儿沐浴室', type: 'AMap.Polygon', data: { path: [[2880,732],[3240,732],[3240,1220],[2880, 1220]] },  part: ObjectId("583690be42aa10288050037a")});
db.drawable.insert({title: '楼梯', type: 'AMap.Polygon', data: { path: [[4680,732],[5040,732],[5040,1220],[4680, 1220]] }, part: ObjectId("583690be42aa10288050037a")});
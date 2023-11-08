---1.以SQL的形式把几个特定的常数查询出来作为一个表
SELECT *
FROM (
  VALUES
    (1, 'Value1'),
    (2, 'Value2'),
    (3, 'Value3')
) AS tmp_tb (id, val);
#!/usr/bin/env sh
<<INFO
SCRIPT:shell_01_specs.sh
AUTHOR:haozhifeng
DATE:2024-01-05
DESCRIBE:haozhifeng的Shell编程规范
INFO


<<INFO
Shell编程规范是确保Shell脚本清晰、可读、易于维护和一致性的重要指南。
以下是一些常见的Shell编程规范建议：
  （1）首行约定，开头要有shebang指定shell解释器,格式如：#!/bin/bash，推荐使用：#!/usr/bin/env bash
      1）在计算机科学中，Shebang（也称为 Hashbang ）是一个由井号和叹号构成的字符序列 #! ，其出现在文本文件的第一行的前两个字符。 它指明了当我们没有指定解释器的时候默认的解释器。
        在文件中存在 Shebang 的情况下，类 Unix 操作系统的程序载入器会分析 Shebang 后的内容，将这些内容作为解释器指令，并调用该指令，并将载有 Shebang 的文件路径作为该解释器的参数[1]。
      2）shell解释器有很多种，除了bash之外，我们可以⽤下⾯的命令查看本机⽀持的解释器:$ cat /etc/shells
        当我们直接使⽤./a.sh来执⾏这个脚本的时候，如果没有shebang，那么它就会默认⽤$SHELL指定的解释器，否则就会⽤shebang指定的解释器。
　　  3）shell脚本是逐行解释执行的,在遇到第一行是 #!/bin/bash 的时候就会加载 bash 相关的环境,在遇到 #!/bin/sh 就会加载 sh 相关的环境,避免在执行脚本的时候遇到意想不到的错误。
        但一开始我并不知道我电脑上安装了哪些shell,默认使用的又是哪一个shell,我脚本移植到别人的计算机上执行,我更不可能知道别人的计算机是Ubuntu还是Arch或是Centos。
        为了提高程序的移植性,本shell规约规定使用 #!/usr/bin/env bash, #!/usr/bin/env bash 会自己判断使用的shell是什么,并加载相应的环境变量。

  （2）文件开头：文件开头（在shebang之后），以多行注释的形式添加脚本文件注释，包括脚本的名称、作者、版本、创建日期、修改日期和用途等信息。

  （3）缩进：使用缩进来表示代码块，提高代码的可读性。使用两个空格进行缩进,不使用用tab缩进

  （4）注释：在关键部分和复杂逻辑处添加注释，解释代码的作用和实现方式。注释应该简洁明了，避免冗余信息。
      1）单行注释以#号开头
      2）多行注释表示方法以 <<INFO 开头，换行书写注释内容，然后以INFO结尾；INFO可以用别的标识代替，但需与结尾保持一致
      3）文件开头（在shebang之后），以多行注释的形式添加脚本文件注释，包括脚本的名称、作者、版本、创建日期、修改日期和用途等信息。
      4）函数必须有注释标识该函数的用途、入参变量、函数的返回值类型。
        函数的注释 # 顶格写, 井号后面紧跟一个空格 ,对于该格式的要求是为了最后生成函数的帮助文档是用的(markdown语法),然后是注释的内容,注释尽量简短且在一行,最后跟的是函数的类型。
      5）函数内注释 # 与缩进格式对整齐
      6）变量的注释紧跟在变量的后面

  （5）命名约定：为变量和函数使用有意义的名称，并遵循一定的命名约定。例如，使用小写字母和下划线来命名变量，函数名全部使用小写字母。
      1）命名只能使用字母,数字和下划线,首个字符不能以数字开头。
      2）中间不能有空格,不能使用标点符号,不能使用汉字,可以使用下划线 _ ,所以我们往往使用 _ 作为分词的标识 例如 user_name、city_id 等等
      3）不能使用bash里的关键字(可用help命令查看保留关键字)。
      4）脚本中的所有变量风格统一使用下划线命名风格。(不强制,视情况而定)
        统一的风格是好的编程习惯的开始,这样程序给人一种清爽的感觉,至于使用驼峰格式还是使用下划线格式,仁者见仁智者见智。
        对比一下就感觉 userName 比 user_name 简洁; 函数名 log_info 比 logInfo 更加贴切,
        我们Java中打印日志的时候一般这样写 log.info("") 或者 log.error(""),所以在shell中使用 log_info "" 这种写法更像Java的习惯;
        getUserInfo 比 get_user_info 更加紧凑,使用起来更像Java的函数命名风格。
        虽然这样,但是本规约还是规定使用下划线风格(特殊情况特殊处理,不强制),因为你可以看看Linux自带的一些脚本,定义变量的时候都使用下划线分割。

  （6）关于变量：
    1）变量名由字母、数字、下划线组成， 只能以字母、下划线开头
    2）尽量减少全局变量，可在变量前面加local使其成为局部变量
      注：全局变量仅在当前Shell有效，使用export定义的全局变量在所有子进程中依然有效
    3）环境变量和全局变量大写，局部变量小写（使用下划线连接，如host_ip ）
    4）引用变量用${value}
    5）不能被清除和修改的变量通过readonly variable声明
    6）常用变量集中写在脚本开头,便于修改
    7）对变量赋空值时，建议使用unset
    8）用ln创建软链接文件，必须先判断文件是否存在，存在时必须先删除，然后再创建软链接
        [ -h /data ] && rm -rf /data
        ln -sf /home/data /data
    9）命令替换推荐使用$()，并用双引号括起来，在groovy中建议使用``(反撇号)
        local_ip="$(ip addr | grep ......)"
    10）shell 中变量的基本类型就是 String 、数值(可以自己看做 Int、Double之类的) 、Boolean。Boolean 其实是 Int类型的变种, 在shell中 0代表真、非0代表假。
    11）定义在函数中的我们称之为函数局部变量;定义在函数外部，shell脚本中变量我们称之为脚本全局变量
    12）关于环境变量。 所有的程序，包括shell启动的程序，都能访问环境变量，有些程序需要环境变量来保证其正常运行。必要的时候shell脚本也可以定义环境变量。


  （7）关于函数：
    1）函数基本规范
      a）函数首字母大写，并用“_”隔开，如Modify_Ip
      b)函数名后面必须加小括号()
      c）第一个大括号与小括号之间保留一个空格
      d)第二个大括号顶格单独占一行
      e)同级别的代码块要左对齐
    2）函数定义格式
      linux shell 可以用户定义函数，然后在shell脚本中可以随便调用。Shell 函数定义的语法格式如下：
        [function] funname [()]{
            函数体
            [return int;]
        }
    a）格式1：简化写法，不写 function 关键字：
      函数名(){
          函数体
      }
    b）格式2：这是标准写法，也推荐大家使用的写法：
      function 函数名(){
        命令序列
      }
    c）格式3：如果写了 function 关键字，也可以省略函数名后面的小括号：
      function 函数名{
        命令序列
      }
    说明：
      I）function 是 Shell 中的关键字，专门用来定义函数；可以带function funname () 定义，也可以直接funname () 定义，不带任何参数；
      II）funname 是函数名；
      III）函数体 是函数要执行的代码，也就是一组语句；
      IV）return int 表示函数的返回值，其中 return 是 Shell 关键字，专门用在函数中返回一个值；这一部分可以写也可以不写，如果不加，将以最后一条命令运行结果，作为返回值。
    使用关键字 function 显示定义的函数为 public 的函数,可以供 外部脚本以 sh 脚本 函数 函数入参 的形式调用
    未使用关键字 function 显示定义的函数为 privat 的函数, 仅供本脚本内部调用,注意这种privat是人为规定的,并不是shell的语法,不推荐以 sh 脚本 函数 函数入参 的形式调用,注意是不推荐而不是不能。
    在函数内部首先使用有意义的变量名接受参数,然后在使用这些变量进行操作,禁止直接操作$1,$2 等，除非这些变量只用一次

  （8）命令执行：在脚本中执行命令时，使用反引号(``)或$()来执行命令并捕获输出结果。

  （9）文件检查：在脚本中处理文件时，先检查文件是否存在、可读或可写，以避免出现错误。

  （10）shell脚本的执行
    假设在 /tmp 目录下新建了shell脚本：test.sh。可以有如下方式运行：
      1）使用终端直接执行脚本
        注：不论是如下在当前路径还是任意路径直接执行脚本，都需要确保在执行脚本之前，脚本文件具有可执行权限。
        a）进入脚本所在的目录，执行./test.sh
          I）.代表当前路径，/的意思就是在当前路径下执行test.sh。如果不加./，bash就会去PATH环境变量里查找，若查找不到，会报错找不到命令。
          II）这种方式，确保在执行脚本之前，脚本文件具有可执行权限。可以使用`chmod`命令来设置脚本的权限。
        b）以绝对路径的方式去执行Shell脚本
          /tmp/test.sh
      2）使用bash命令执行脚本
        bash /tmp/test.sh
      3）使用sh命令执行脚本
        类似于使用`bash`命令执行脚本，也可以使用`sh`命令来执行脚本。在终端中执行以下命令：
        sh /tmp/test.sh
      4）使用source命令执行脚本：
        如果希望在当前shell环境中执行脚本，可以使用`source`命令。在终端中执行以下命令：
          source /tmp/test.sh
        或者可以使用`.`运算符来代替`source`命令：
          . /tmp/test.sh
        这将在当前shell环境中执行脚本，并将其中的命令应用于当前shell。
      如果想直接运行一个shell脚本（不考虑在其他shell脚本中采用4）执行的方式），相比于2）、3），更推荐使用方式1）执行shell脚本
   规范只是一些常见的建议，具体实现可能因项目和团队的不同而有所差异。重要的是保持一致性和清晰的代码风格，以提高脚本的可读性和可维护性。
INFO

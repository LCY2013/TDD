# TDD(Test-Driven Development) 特点

### 第一是，将要完成的功能分解成一系列任务，再将任务转化为测试，以测试体现研发进度，将整个开发过程变成有序的流程，以减少无效劳动。

### 第二是，在修改代码的时候，随时执行测试以验证功能，及时发现错误，降低发现、定位错误的成本，降低修改错误的难度。

### 第三是，时刻感受到认知的提升，增强自信降低恐惧。在针对列表参数使用任务分解法时，你明显可以感觉到，我们无论是对需求的把握性，还是对最终实现的可预见性，都有了大幅度的提升。甚至，如果更进一步要求，我们可以较有把握地评估（误差在 15% 以内）实现列表参数解析需要多长时间。这就是我们认知提升的具体体现。

## 这样的工作状态称为“职业程序工作状态”：有序、可控、自信。

## TDD编程方式

### 使用有意义的方法名代替注释

- || 不同的条件抽取到不同的方法

- && 条件通常抽取到同一个方法里


## 状态验证为什么是主要的使用方式?

### 测试的基本结构

无论使用什么样的测试框架，每个测试都由四个依次执行的阶段组成：初始化（SetUp）、执行测试（Exercise）、验证结果（Verify）和复原（Teardown）。如下图所示：

![测试的基本结构](images/测试的基本结构.png)

- **初始化。**主要是设置测试上下文，从而使待测系统（System Under Test）处于可测试的状态。例如，对于需要操作数据库的后台系统，测试上下文包含了已经灌注测试数据的测试用数据库，并将其与待测系统连接。

- **执行测试。**就是按照测试脚本的描述与待测系统互动。例如，按照功能描述，通过 API 对系统进行相应的操作。

- **验证结果。**就是验证待测系统是否处于我们期待的状态中。例如，经过测试，数据库中的业务数据是否发生了期待中的改变。

- **复原。**就是将测试上下文、待测系统复原回测试之前的状态，或者消除测试对于待测系统的副作用。例如，删除测试数据中的数据（通常是通过事务回滚）。

在测试的四个步骤中，验证结果是最核心的一步，也是最核心的技术。

验证结果有两种方式：**状态验证（State Verification）**和**行为验证（Behavior Verification）**。

### 状态验证
状态验证是指在与待测系统交互后，通过比对测试上下文与待测系统的状态变化，判断待测系统是否满足需求的验证方式。

状态验证是一种黑盒验证，它将测试上下文与待测系统当作一个整体。当待测系统不存在内部状态，而通过作用于依赖组件（Depended On Component）达成功能的时候，我们会从依赖组件中获取状态，以验证待测系统。如下图所示：

![测试的基本结构](images/测试的基本结构2.png)

状态验证的一个难点是复原测试上下文，消除因执行测试造成的状态累积。如果待测系统是类库，一般问题不大，重新构造新的对象实例就行了。

而对于测试环境中存在进程外组件的情况，问题就要复杂一些了。在这种情况下，增量状态验证（Delta Verification）是一种有效的手段。

在每一个测试复原时，都使用了“drop-and-create”来清除数据库中的数据，从而消除状态累积。但如果因为种种原因（比如测试数据量很大），使得每次清除测试数据都变得不现实时，就可以使用增量状态验证来降低状态累积的影响。

### 行为验证(行为验证为什么应该尽量避免使用？)
行为验证是指通过待测系统与依赖组件（Depended On Component）的交互，来判断待测系统是否满足需求的验证方式。其验证方式如下图所示：

![img.png](images/行为验证验证方式.png)

行为验证背后的逻辑是，状态的改变是由交互引起的。如果所有的交互都正确，那么就可以推断最终的状态也不会错。例如，对于如下的代码：

```java
interface Counter {
  void increase(); 
}

class SUT {
  public void action(Counter counter) {
    counter.increase();
  }
}
```

功能需求是 SUT 的 action 方法调用计数器 Counter 使其计数增加。按照状态验证，我们需要从 Counter 中获取内部计数，然后判断在执行测试前后，计数是否增加。

而对于行为验证，因为计数增加与否只在于是否调用了 increase 方法。那么如果 SUT 调用了 increase 方法，我们就可以推测Counter 的计数也必然增加了。于是，我们可以将对于读数增加的验证，转化为对于 increase 方法调用的验证：如果 increase 方法被调用了，那么我们可以推测SUT 是满足功能需求的。状态验果，行为推因。

对比状态验证和行为验证，在测试上下文中，引入了测试替身（Test Double）来作为测试上下文中的依赖组件，然后使用测试替身的验证方法代替了测试框架提供的断言方法。具体到项目例子中，使用 Mockito API 中的 verify 替换了 JUnit API 中的 assert 方法。

### 行为测试 VS 状态测试
行为验证在特定场景有用，但是会损失测试的有效性，TDD还是以状态验证为主，在容器时代mock会变得容易很多。

## 集成测试还是单元测试？

![img.png](images/集成测试还是单元测试.png)

如上图所示，第一个测试的测试范围包含了 Args 和 OptionParser，可以看作是“集成测试”或“功能测试”。第二个测试的测试范围仅仅覆盖 OptionParser，才会被看作是“单元测试”。

这个 OptionParser 接口并不是一开始就存在，而是通过重构获得的。在重构的过程中，我们从 Args 中分离了部分代码，从而使得 ArgsTest 的测试范围不再聚焦于某一个单元，而是覆盖了更多的单元。

于是 ArgsTest 就成了“集成测试”。更有意思的是，在重构的过程中，虽然这个测试没有任何变动，但是到重构结束的时候，它就从“单元测试”变成了“集成测试”。

### TDD 中的单元测试
在 TDD 的语境下，“单元测试”指的是能提供快速反馈的低成本的研发测试（Developer Test）。TDD 社区希望通过强调“单元测试”，来强调这些特点。但是“单元测试”这个词，并不是由 TDD 社区发明的，而是软件行业中由来已久的一个词汇。在不做任何强调的情况下，它会指针对不涉及进程外组件的单一软件单元的测试。

- TDD 中的测试是由不同粒度的功能测试构成的；

- 每一个测试都兼具功能验证和错误定位的功效；

- 要从发现问题和定位问题的角度，去思考测试的效用与成本；

- 单元粒度要以独立的功能上下文或变化点为粒度。

### TDD应该作为分解功能进行代码开发，而非测试方法，所以TDD关注点不该在单元测试这种专业的测试术语上，而是不同粒度的功能测试。

## 驱动的极限是什么？

从“驱动”的角度来说，TDD 实际上并不是一种编码技术（Coding Technique），它无法帮助实现你不会写的代码，你必须要知道如何实现这些功能；但是一旦你明确了要实现的功能，并且知道要怎么实现，TDD 可以帮助你更好地将功能放置到不同的单元。也就是说，TDD“驱动”的是架构，因而实际是一种架构技术。

是的，这正是我们讲的编码架构师（Coding Architect），也是真正的实干型而非 PPT 型架构师。这也是为什么 TDD 也被看作 Test Driven Design。

### 重构的手法
四种常用的重构手法，分别是提取方法、内联方法、引入参数和引入字段。并区分了用于高效修改代码的重构手法，和用于消除坏味道以改进架构设计的重构。还介绍了一种架构软件的方法：重构到模式。也就是通过重构将坏味道替换为模式，从而改进软件架构的方式。

## [TDD中的经典学派(Classic School)](https://github.com/LCY2013/TDD/application-classic) 与 [伦敦学派(London School)](https://github.com/LCY2013/TDD/application-london)
重构到模式，或者说 TDD 红 / 绿 / 重构循环中的重构，是在完成功能的前提下以演进的方式进行设计。这是延迟性决策策略，也叫最晚尽责时刻（Last Responsible Moment，LRM）。也就是说，与其在信息不足的情况下做决定，不如延迟到信息更多，或是不得不做出决策的时机再决策。这种策略的重点在于，在保持决策有效性的前提下，尽可能地推迟决策时间。

如果架构愿景不清晰，那么“最晚尽责时刻”让我们不必花费时间进行空对空的讨论，可以尽早开始实现功能，再通过重构从可工作的软件（Working Software）中提取架构。这种方式也被称作 TDD 的经典学派（Classic School）或芝加哥学派（Chicago School）。

除了经典学派之外，还有一种 TDD 风格，被称作 TDD 的伦敦学派（London School）。如果架构愿景已经比较清晰了，那么我们就可以使用伦敦学派进行 TDD。

### 伦敦学派的做法
- 按照功能需求与架构愿景划分对象的角色和职责；

- 根据角色与职责，明确对象之间的交互；

- 按照调用栈（Call Stack）的顺序，自外向内依次实现不同的对象；

- 在实现的过程中，依照交互关系，使用测试替身替换所有与被实现对象直接关联的对象；

- 直到所有对象全部都实现完成。

### 经典学派做法
- 经典学派强调功能优先，设计 / 架构后置，通过重构进行演进式设计。

- 而“伦敦学派”并不排斥预先存在的设计，更强调如何通过测试替身，将注意力集中到功能上下文中的某个对象上。然后在测试的驱动下，按部就班地完成功能开发。

可以将伦敦学派看作一种利用架构愿景分割功能上下文，然后再进入经典模式的 TDD 方法。这么做的好处是，对于复杂的场景，可以极大简化构造测试的时间。

经典学派和伦敦学派是 TDD 中都需要掌握的基本功。在功能上下文内，以经典学派为主；而跨功能上下文时，可以使用伦敦学派对不同的功能上下文进行隔离。

- 经典学派质疑伦敦学派向预先设计妥协，放弃了演进式架构。

- 伦敦学派攻击经典学派不愿吸收借鉴行业积累，“难道每次都要从一个大类重构到 MVC 吗？”

然而这种分歧是无意义的。在不同的场合下，我们只需要使用该用的方式就好。

### 测试驱动到底能够驱动什么？
单元级别功能测试能够驱动其对应单元（功能上下文或变化点）的功能需求。而对于单元之内某个功能的实现，则无能为力。

从“驱动”的角度来说，TDD 实际上并不是一种编码技术，更像是一种架构技术，它可以帮助你更好地将功能放置到不同的单元。于是我们继续介绍了 TDD 中两种处理架构的思路：延迟性决策的经典学派和通过架构愿景划分功能上下文的伦敦学派。

## 工程化方法的TDD：更低的成本与更高的效能
![img.png](images/工程化方法的TDD.png)

如上图所示，使用 TDD 的核心流程为：

- 首先将需求分解为功能点，也就是将需求转化为一系列可验证的里程碑点；

- 如果已经存在架构或架构愿景，则依据架构中定义的组件与交互，将功能点分解为不同的功能上下文；

- 如果尚不存在架构愿景，则可以将功能点作为功能上下文；

- 将功能点按照功能上下文，分解为任务项。也就是进一步将可验证的里程碑点，分解为功能上下文中可验证的任务项；

- 将任务项转化为自动化测试，进入红 / 绿 / 重构循环，驱动功能上下文内的功能实现；

- 如果重构涉及功能上下文的重新划分，即提取 / 合并组件，即视作对于架构的重构与梳理。需调整后续功能点中对于功能上下文以及任务项的划分。

- 如此往复，直到所有功能完成。

通过上述过程的描述，可以发现任务列表中的任务项源自两层分解：源自对于业务理解的功能点分解，以及源自架构愿景的功能上下文分解。

功能点分解帮助我们形成可验证的里程碑点。这些里程碑点可看作由可工作的软件（Working Software）构成的进度度量。功能上下文分解帮助我们找到正确的单元，指导我们保持良好的软件架构。

如果功能点分解错误，那么就得不到功能正确的软件；如果功能上下文分解错误，那么就得不到架构良好的软件。

### TDD 的工程优势
所以，使用 TDD 开发软件对人的要求，与其他所有软件工程方法对人的要求是一样的：理解需求，明白架构。但是 TDD 提供了这样几点在工程管理上的优势。

第一，理解需求等于可以针对功能点写出测试。换句话说，写不出测试就是不理解需求。不理解需求就不要开发。在不理解需求的前提下开发功能点，只能带来负的进度。从工程管理角度上看，“判断一个人是否理解了需求”的成本极高。我想大家都有在各种拉通会议中，反复宣讲确认，但仍如鸡同鸭讲，不断消磨精力与斗志的体验。

TDD 的“先写测试，后写生产的代码”的实践，实际上也就是要求“先理解需求，再开发”。但是 TDD 对于什么叫理解了需求，有了清晰的要求：能写出这个功能点的测试，就是理解了需求，别的都白扯。因而 TDD 从工程管理的角度，降低了“判断一个人是否理解了需求”的成本：看测试就行。

第二，不写测试，除了不会写测试之外，就是没理解需求。没理解需求就去写测试，那就是瞎干，瞎干不如不干。如果整个团队都写不出测试，那么说明这个需求无法通过可管控的工程化方式交付。

可管控的工程化方式交付，意味着这个需求在实现层面上可以被执行，也就是高确定性的。在高确定性的环境下，要追求效率。

而无法通过可管控的工程化方式交付，意味着不确定这个需求在实现层面上是否可被执行，需要进入探索模式。在不确定的情况下，要追求低成本及时止损。

所谓探索模式，也就是原型验证、定向的深度搜索、聘请外脑之类的活动。探索模式的关键在于，要在给定的时间范围内尝试，比如一到两天。超出这个时间范围仍然无法解决的，将变成项目风险，需要额外的管理方法，比如上升。然而，从工程管理的角度，“判断哪个需求应该从工程化方式交付模式进入探索模式”的成本极高。通常已经形成进度阻塞、延期或大量返工的时候，才能发现根本原因是不能确定实现方式。

TDD 可以通过是否可以在规定时间内写出测试加以判断。因而，从工程管理的角度，降低了“判断哪个需求应该从工程化方式交付模式进入探索模式”的成本：看测试就行。

第三，所有软件从业人士都认为架构是重要的，但却很少有人理解架构究竟是如何发挥作用的。架构并不是停留在纸面上的框图，而是约定了构成软件系统的组件，以及组件之间的交互方式。也就是说，架构是组件职责划分的依据以及组件的交互模式。

![img.png](images/TDD的工程优势.png)

想要破坏 MVC 的架构愿景很容易，比如把控制逻辑写到模型中，或者在视图中直接修改模型，都会破坏 MVC 的架构愿景。

架构是软件构造过程中产生的一类非常特殊的知识，它必须成为全体人员的共识，才能真正发挥作用。否则，架构就是一句空话，或者一堆空话。

从工程管理的角度，“判断团队是否对架构达成了共识”的成本极高。通常需要大量的代码审查才能发现对于架构理解的偏差，也需要大量的返工才能纠正这些偏差。

TDD 则可以通过功能上下文以及任务项拆分的情况，判断成员是否认同并理解了架构。如果团队已经形成了架构共识，那么对于相同的功能点，团队中所有成员拆分出的功能上下文应该也相同。因而，从工程管理的角度，TDD 降低了“判断团队是否对架构达成了共识”的成本：看功能上下文拆分就行。

第四，架构愿景很难在一开始就想得尽善尽美，随着需求发展，总会出现以当前架构愿景不容易实现的需求。如果硬拗进当前架构，就会出现不当的职责划分和别扭的组件交互，这只会加速架构的腐化。而“发现当前架构愿景不容易实现的需求”成本极高。

TDD 则可以通过功能上下文以及任务项拆分的情况，判断架构是否能够实现当前需求。如果无法拆分出合理的功能上下文和任务项，那么这个需求，就是当前架构愿景不易实现的需求。因而，从工程管理的角度，TDD 降低了“发现当前架构愿景不容易实现的需求”的成本：看功能上下文拆分就行。

顺便说一句，就算发现了“当前架构愿景不容易实现的需求”，也可以直接进入经典模式，通过重构抽取架构要素，演进式调整当前架构，不会影响项目进度。如下图所示：

![img.png](images/演进式调整当前架构.png)

不难发现，TDD 要求先写测试，实际是以可工作的软件的形态，验证对于需求的理解；而任务分解，则是在确定单元粒度的过程中，验证对于架构的共识。只要我们承认，“理解需求，明白架构”是一切工程化软件开发的前提，那么 TDD 就是目前已知的效能最高的方法。

## [如何实现基本的DI容器结构?](https://github.com/LCY2013/TDD/di-container)

### 将需求分解为功能点
对于组件构造部分，分解的任务如下：

- 无需构造的组件——组件实例

- 如果注册的组件不可实例化，则抛出异常
  - 抽象类
  - 接口

- 构造函数注入
  - 无依赖的组件应该通过默认构造函数生成组件实例
  - 有依赖的组件，通过 Inject 标注的构造函数生成组件实例
  - 如果所依赖的组件也存在依赖，那么需要对所依赖的组件也完成依赖注入
  - 如果组件有多于一个 Inject 标注的构造函数，则抛出异常
  - 如果组件需要的依赖不存在，则抛出异常
  - 如果组件间存在循环依赖，则抛出异常
  
- 字段注入
  - 通过 Inject 标注将字段声明为依赖组件
  - 如果组件需要的依赖不存在，则抛出异常
  - 如果字段为 final 则抛出异常
  - 如果组件间存在循环依赖，则抛出异常

- 方法注入
  - 通过 Inject 标注的方法，其参数为依赖组件
  - 通过 Inject 标注的无参数方法，会被调用
  - 按照子类中的规则，覆盖父类中的 Inject 方法
  - 如果组件需要的依赖不存在，则抛出异常
  - 如果方法定义类型参数，则抛出异常
  - 如果组件间存在循环依赖，则抛出异常

对于依赖选择部分，分解的任务列表如下：

- 对 Provider 类型的依赖
  - 注入构造函数中可以声明对于 Provider 的依赖
  - 注入字段中可以声明对于 Provider 的依赖
  - 注入方法中可声明对于 Provider 的依赖

- 自定义 Qualifier 的依赖
  - 注册组件时，可额外指定 Qualifier
  - 注册组件时，可从类对象上提取 Qualifier
  - 寻找依赖时，需同时满足类型与自定义 Qualifier 标注
  - 支持默认 Qualifier——Named

对于生命周期管理部分，分解的任务列表如下：

- Singleton 生命周期
  - 注册组件时，可额外指定是否为 Singleton
  - 注册组件时，可从类对象上提取 Singleton 标注
  - 对于包含 Singleton 标注的组件，在容器范围内提供唯一实例
  - 容器组件默认不是 Single 生命周期

- 自定义 Scope 标注
  - 可向容器注册自定义 Scope 标注的回调



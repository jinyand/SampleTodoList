# SampleTodoList
MVVM, AAC(ViewModel, LiveData, Room), RecyclerView를 이용한 TodoList App

### 0. 세팅
* build.gradle (app)
  ```kotlin
  android {
      ...
      dataBinding {
          enabled = true
      }
  }

  dependencies {
      ...
      // Room
      implementation 'androidx.room:room-runtime:2.2.6'
      implementation "androidx.room:room-ktx:2.2.6"
      kapt 'androidx.room:room-compiler:2.2.6'

      // Lifecycle (coroutine)
      implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.4.0-alpha01'
      implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0-alpha01'
  }
  ```
  
  
### 1. Room
* Entity - Todo.kt : id, title, description 필드를 가진 data class
* DAO - TodoDao.kt : 데이터베이스에 접근하기 위한 인터페이스
* RoomDatabase - TodoDatabase.kt : 실제 접근되는 데이터베이스


### 2. Repository
<img src="https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbE6cyr%2FbtqDp1AGJnf%2FckqREtjHnKfzbtM4frong0%2Fimg.png" width="450" height="400">

* Repository는 데이터, 데이터 소스와 ViewModel 사이의 종속성을 줄여준다. => 데이터를 캡슐화한다.
* 위 그림을 보면 오직 Repository만 여러 요소들과 종속되어있다.
* ViewModel에서는 UI를 업데이트하는데 필요한 데이터를 소유하고 있는데, 이 때 이 데이터들을 Repository에 요청한다.
* 따라서 Repository 패턴을 사용하게 되면 ViewModel은 UI를 업데이트하기 위한 데이터를 제공하는 일에만 집중할 수 있다.

### 3. ViewModel
* ViewModel - MainViewModel.kt : repository 객체를 생성하여 관찰한다.

### 4. Recyclerview Adapter
* item_todo.xml : Textview 데이터 바인딩  
  * title : `tools:text="@{todo.title.toString()}"`  
  * description : `tools:text="@{todo.description.toString()}"`
  
* TodoAdpater.kt
  ```kotlin
  class TodoAdapter(val todoItemClick: (Todo) -> Unit, val todoItemLongClick: (Todo) -> Unit)
      : RecyclerView.Adapter<TodoAdapter.ViewHolder> () {

      private var todos: List<Todo> = listOf()

      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoAdapter.ViewHolder {
          val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
          return ViewHolder(binding)
      }

      ...

      inner class ViewHolder(val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
          fun bind(todo: Todo) {
              binding.todo = todo

              binding.root.setOnClickListener {
                  todoItemClick(todo)
              }

              binding.root.setOnLongClickListener {
                  todoItemLongClick(todo)
                  true
              }
          }
      }

      ...

  }
  ```
  * 기존(데이터바인딩x)에는 onCreateViewHolder에서 item_todo.xml을 inflate하여 반환하지만, 데이터바인딩을 사용하면 ItemTodoBinding이라는 객체를 반환
  * ViewHolder(내부클래스)는 반환된 ItemTodoBinding 객체를 받아서 세팅

# SampleTodoList
MVVM, AAC(ViewModel, LiveData, Room), RecyclerView를 이용한 TodoList App  
TODO 리스트 목록과 TODO 추가의 두 개 액티비티를 포함한다.  
Recyclerview로 TODO 리스트가 나열되고, 새로 추가하면 자동으로 추가/정렬된다.  
리스트의 항목을 한 번 클릭하면 편집, 길게 클릭하면 삭제 다이얼로그를 보여준다.

### 0. Dependency
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
  ```kotlin
  @Entity
  data class Todo(
      // autoGenerate : null을 받으면 자동으로 ID값 할당
      @PrimaryKey(autoGenerate = true)
      var id : Int?,
      @ColumnInfo(name = "title")
      var title: String,
      @ColumnInfo(name = "description")
      var description: String
  ) {
      constructor() : this(null, "", "")
  }
  ```
* DAO - TodoDao.kt : 데이터베이스에 접근하기 위한 인터페이스
  ```kotlin
  @Dao
  interface TodoDao {
      @Query("SELECT * FROM Todo")
      fun getAll(): LiveData<List<Todo>>

      @Insert(onConflict = OnConflictStrategy.REPLACE) // pk 중복처리
      fun insert(todo: Todo)

      @Update
      fun update(todo: Todo)

      @Delete
      fun delete(todo: Todo)
  }
  ```
  * 혹시 여기서 title 이름에 따라 정렬하고 싶다면 getAll()의 Query문에 `BY title ASC` 를 추가하기
  
* RoomDatabase - TodoDatabase.kt : 실제 접근되는 데이터베이스
  ```kotlin
  @Database(entities = [Todo::class], version = 1)
  abstract class TodoDatabase : RoomDatabase() {
      abstract fun todoDao() : TodoDao

      // 싱글톤 패턴
      companion object {
          private var INSTANCE : TodoDatabase? = null

          fun getInstance(context: Context): TodoDatabase? {
              if(INSTANCE == null) {
                  synchronized(TodoDatabase::class) { 
                  // synchronized : 여러 스레드가 동시에 접근 불가, 동기적으로 접근
                      INSTANCE = Room.databaseBuilder(
                          context.applicationContext,
                          TodoDatabase::class.java,
                          "todo"
                      ).fallbackToDestructiveMigration().build()
                  }
              }
              return INSTANCE
          }
      }
  }
  ```
  * fallbackToDestructiveMigration() : 데이터베이스가 갱신될 때 기존의 테이블을 버리고 새로 사용하도록 설정
  * 이렇게 만들어지는 DB 인스턴스는 Repository에서 호출하여 사용할 예정

### 2. Repository
<img src="https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbE6cyr%2FbtqDp1AGJnf%2FckqREtjHnKfzbtM4frong0%2Fimg.png" width="450" height="400">

* Repository는 데이터, 데이터 소스와 ViewModel 사이의 종속성을 줄여준다. => 데이터를 캡슐화한다.
* 위 그림을 보면 오직 Repository만 여러 요소들과 종속되어있다.
* ViewModel에서는 UI를 업데이트하는데 필요한 데이터를 소유하고 있는데, 이 때 이 데이터들을 Repository에 요청한다.
* 따라서 Repository 패턴을 사용하게 되면 ViewModel은 UI를 업데이트하기 위한 데이터를 제공하는 일에만 집중할 수 있다.

```kotlin
class Repository(application: Application) {
    private val todoDatabase: TodoDatabase = TodoDatabase.getInstance(application)!!
    private val todoDao: TodoDao = todoDatabase.todoDao()
    private val todos: LiveData<List<Todo>> = todoDao.getAll()

    fun getAll(): LiveData<List<Todo>> {
        return todos
    }

    fun insert(todo: Todo) {
        try {
            val thread = Thread(Runnable {
                todoDao.insert(todo)
            })
            thread.start()
        } catch (e: Exception) { }
    }

    fun delete(todo: Todo) {
        try {
            val thread = Thread(Runnable {
                todoDao.delete(todo)
            })
            thread.start()
        } catch (e: Exception) { }
    }
}
```
* ViewModel에서 DB에 접근을 요청할 때 수행할 함수를 만들어준다.  
  여기서 주의할 점은 Room DB를 메인 스레드에서 접근하려 하면 크래쉬가 발생한다.  
  따라서 별도의 스레드에서 Room의 데이터에 접근해야 한다.

### 3. ViewModel
* ViewModel - MainViewModel.kt : repository 객체를 생성하여 관찰한다.
```kotlin
class MainViewModel(application: Application): AndroidViewModel(application) {
    private val repository = Repository(application)
    private val todos = repository.getAll()

    fun getAll(): LiveData<List<Todo>> {
        return todos
    }

    fun insert(todo: Todo) {
        repository.insert(todo)
    }

    fun delete(todo: Todo) {
        repository.delete(todo)
    }
}
```
* AndroidViewModel 에서는 Application 을 파라미터로 사용한다.
* Repository를 통해서 Room 데이터베이스의 인스턴스를 만들 때에는 context가 필요하다.  
  하지만, 만약 ViewModel이 액티비티의 context를 쓰게 되면 액티비티가 destroy 된 경우에는 메모리 릭이 발생할 수 있다.  
  따라서 Application Context를 사용하기 위해 Application을 인자로 받는다.
* DB를 제어할 함수는 Repository에 있는 함수를 이용해 설정해준다.

### 4. RecyclerView
* item_todo.xml : Textview 데이터 바인딩  
  * title : `tools:text="@{todo.title.toString()}"`  
  * description : `tools:text="@{todo.description.toString()}"`
  
* activity_main.xml : Recyclerview 세팅

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
  * View에서 화면을 갱신할 때 사용할 setTodos 함수 - 데이터베이스가 변경될 때마다 이 함수가 호출된다.

* MainActivity.kt
  ```kotlin
  class MainActivity : AppCompatActivity() {
      private lateinit var binding: ActivityMainBinding
      private val mainViewModel: MainViewModel by viewModels()
  
      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

          binding.viewModel = mainViewModel
          binding.lifecycleOwner = this

          setRecyclerView()

          binding.btnAdd.setOnClickListener {
              val intent = Intent(this, AddActivity::class.java)
              startActivity(intent)
          }

      }
   ```
   * **by viewModels()** 를 사용하면 ViewModelProvider를 사용하지 않고 viewmodel을 지연 생성할 수 있다. (ktx dependency 추가)
   * 즉, `mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)` 의 코드와 동일
   
   ```kotlin
      private fun deleteDialog(todo: Todo) {
          val builder = AlertDialog.Builder(this)
          builder.setMessage("Delete selected item?")
              .setNegativeButton("취소") { _, _ -> }
              .setNeutralButton("삭제") {_, _, ->
                  mainViewModel.delete(todo)
              }
          builder.show()
      }

      private fun setRecyclerView() {
          // Set contactItemClick & contactItemLongClick lambda
          // click = put extras & start AddActivity
          // longclick = delete dialog
          val adapter = TodoAdapter ({todo -> goAddActivity(todo)}, {todo -> deleteDialog(todo)})

          binding.apply {
              rvMain.adapter = adapter
              rvMain.layoutManager = LinearLayoutManager(applicationContext)
          }

          mainViewModel.getAll().observe(this, Observer { todos ->
              adapter.setTodos(todos!!)
              // adpater를 통해 ui 업데이트
          })
      }

      private fun goAddActivity(todo: Todo) {
          val intent = Intent(this, AddActivity::class.java)
          intent.putExtra("title", todo.title)
          intent.putExtra("desc", todo.description)
          intent.putExtra("id", todo.id)
          startActivity(intent)
      }
  }
  ```
  * deleteDialog : 아이템을 길게 눌렀을 때 뜨는 다이얼로그
  * setRecyclerView : 여기서 Adapter를 선언할 때 onClick 과 onLongClick 시에 해야할 일을 각각 `(Todo) -> Unit` 파라미터로 넘겨주어야 한다.  
  onClick 시에는 AddActivity로 이동, onLongClick 시에는 deleteDialog를 호출한다.
  * goAddActivity : 클릭한 아이템의 정보를 intent에 담아 AddActivity로 이동


### 5. AddActivity
* Todo를 추가할 수 있는 액티비티
```kotlin
class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding
    private val mainViewModel: MainViewModel by viewModels()
    private var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add)
        
        // intent에서 받아온 item 정보를 setText로 지정
        if (intent != null && intent.hasExtra("title") && intent.hasExtra("desc") && intent.hasExtra("id")) {
            binding.etTitle.setText(intent.getStringExtra("title"))
            binding.etDesc.setText(intent.getStringExtra("desc"))
            id = intent.getIntExtra("id", -1)
        }

        binding.apply {
            btnDone.setOnClickListener {
                val title = etTitle.text.toString()
                val desc = etDesc.text.toString()

                if (etTitle.text.isNotEmpty() && etDesc.text.isNotEmpty()) {
                    val todo = Todo(id, title, desc)
                    Log.d("테스트", title)
                    mainViewModel.insert(todo)
                    finish()
                } else {
                    Toast.makeText(applicationContext, "빈 칸을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}
```
* 아이디 값이 null일 경우 Room에서 자동으로 id를 생성해주면서 새로운 Todo를 DB에 추가한다.
* id값을 MainActivity에서 intent로 받아온 경우, 해당 id값의 아이템을 수정하게 된다.  
  (DAO에서 OnConflictStrategy를 REPLACE로 설정해뒀기 때문)
  

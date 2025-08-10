import './App.css'
import {useEffect, useState} from "react";
import SingleTodoEntity from "./SingleTodoEntity.jsx";

function App() {

  useEffect(() => {
    async function fetchData() {
      const url = "/api/todos"
      try {
        const response = await fetch(url)

        if (!response.ok) {
          updateOverlay(true, "Unable to get a list of todos", '')
          return
        }

        const result = await response.json();
        return result;
      } catch (error) {
        console.log(error);
      }
    }
    fetchData().then(r => setTodos(r));
    const eventSource = new EventSource("/api/todos/sse")
    eventSource.addEventListener("todo-deleted", (event) => {
      const idToRemove = JSON.parse(event.data).id;
      setTodos(todos =>
        todos.filter(todo => todo.id !== idToRemove)
      )
    })
    eventSource.addEventListener("todo-created", (event) => {
      setTodos(todos => [JSON.parse(event.data), ...todos])
    })
    eventSource.addEventListener("todo-updated", (event) => {
      const updatedTodo = JSON.parse(event.data)
      setTodos(todos =>
        todos.map(todo => todo.id === updatedTodo.id ? { ...updatedTodo } : todo)
      )
    })
    return () => eventSource.close()
  }, [])

  async function updateCompletedState(id, newState) {
    const url = `/api/todos/${id}`
    const response = await fetch(url, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({completed: newState})
    })

    if (!response.ok) {
      updateOverlay(true, "Unable to update a todo status", '')
    }
  }

  async function updateTodoTitle(id, newTitle) {
    const url = `/api/todos/${id}`
    const response = await fetch(url, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({title: newTitle})
    });

    if (!response.ok) {
      throw await response.json()
    }
  }

  async function handleTodoCreation(e) {
    e.preventDefault();
    if (todos == null)
      return
    const todoTitle = newTodoTitle;
    if (todoTitle.trim().length === 0) {
      return
    }
    setTodoTitle('');
    await createNewTodo(todoTitle)
  }

  async function createNewTodo(title) {
    const url = "/api/todos";
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({title: title})
    });

    if (!response.ok) {
      const responseJson = await response.json();
      if (responseJson.error === "TodoAlreadyExistsException") {
        updateOverlay(true, 'Wrong title', `The todo with title '${title}' already exists`);
      } else {
        updateOverlay(true, "Unable to create a new todo", '')
      }
    }
  }

  async function deleteTodo(id) {
    const url = `/api/todos/${id}`
    const response = await fetch(url, {
      method: "DELETE",
    });

    if (!response.ok) {
      updateOverlay(true, `Unable to delete todo number ${id}`, '')
    }
  }

  function updateOverlay(isOpen, title, description) {
    setOverlay(prev => ({
      ...prev,
      isOpen: isOpen,
      title: title,
      description: description,
    }))
  }

  const [todos, setTodos] = useState([])
  const [newTodoTitle, setTodoTitle] = useState('')
  const [overlay,  setOverlay] = useState({
    isOpen: false,
    title: 'Title',
    description: 'Description',
  })

  return (
    <>
      {overlay.isOpen && (<div className="overlay">
        <div className="overlayContent">
          <h3 className="overlayTitle">{overlay.title}</h3>
          <p className="overlayDescription">{overlay.description}</p>
          <button className="overlayButton" onClick={() => setOverlay(prev => ({...prev, isOpen: false}))}>Close</button>
        </div>
      </div>)}
      <div className="mainContainer">
      <h1 className="applicationName">Yet another todo app</h1>
      <form className="newTodoContainer" onSubmit={handleTodoCreation}>
        <input
          name="todoTitle"
          className="inputField"
          placeholder="Enter new todo name"
          value={newTodoTitle}
          onChange={(event) => setTodoTitle(event.target.value)}
        />
        <button type="submit" className="addTodoButton">Add</button>
      </form>
      <div className="todosContainer">
        { todos != null &&
          (todos.map(item =>
          <SingleTodoEntity
            key={item.id}
            id={item.id}
            name={item.title}
            completed={item.completed}
            updateCompleted={updateCompletedState}
            updateTitle={updateTodoTitle}
            deleteTodo={deleteTodo}
            showDialog={updateOverlay}
          />
        ))
        }
      </div>
    </div>
    </>
  )
}

export default App

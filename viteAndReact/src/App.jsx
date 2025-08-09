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
      return
    }

    const updatedTodo = await response.json()

    setTodos(todos =>
      todos.map(todo => todo.id === updatedTodo.id ? updatedTodo : todo)
    )
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

    const updatedTodoTitle = await response.json();

    setTodos(todos =>
      todos.map(todo => todo.id === updatedTodoTitle.id ? updatedTodoTitle : todo)
    )
  }

  // async function updateTodo(id) {
  //   const url = `/api/todos/${id}`
  //   await fetch(url)
  //   .then(r => r.json())
  //   .then(fetchedTodo =>
  //     setTodos(todos =>
  //       todos.map(todo =>
  //         todo.id === fetchedTodo.id ? fetchedTodo : todo)
  //     )
  //   ).catch(e => console.log(e));
  // }

  async function handleTodoCreation(e) {
    e.preventDefault();
    if (todos == null)
      return
    const todoTitle = newTodoTitle;
    if (todoTitle.trim().length === 0) {
      return
    }
    setTodoTitle('');
    createNewTodo(todoTitle)
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

    if (response.ok) {
      const newTodo = await response.json();

      setTodos(todos => [newTodo, ...todos])
    } else {
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

    if (response.ok) {
      setTodos(todos =>
      todos.filter(todo => todo.id !== id)
     )
    } else {
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

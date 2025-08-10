import './SingleTodoEntity.css'
import {useEffect, useRef, useState} from "react";

function TodoEntity({id, name, completed, updateCompleted, updateTitle, deleteTodo, showDialog}) {
  const [todoTitle, setTodoTitle] = useState(name);
  const inputRef = useRef(null);

  useEffect(() => {
    setTodoTitle(name);
  }, [name]);

  async function handleTitleUpdate() {
    try {
      if (todoTitle !== name) {
        await updateTitle(id, todoTitle)
      }
    } catch (e) {
      setTodoTitle(name)
      if (e.error === "TodoUnexpectedSyntax") {
        showDialog(true, 'Wrong title', "The title cannot be empty");
        return
      } else if (e.error === "TodoAlreadyExistsException") {
        showDialog(true, 'Wrong title', `The todo with title '${todoTitle}' already exists`);
        return
      }
      showDialog(true, 'Unable to update todo title', '');
    }
  }

    return (
        <div className="todoBox">
          <input
            type="checkbox"
            name="completed"
            checked={completed}
            onChange={() => updateCompleted(id, !completed)}
            className="completedButton"
          />
          <input
            name="todoTitle"
            ref={inputRef}
            type="text"
            value={todoTitle}
            className="titleInputField"
            onChange={(e) => setTodoTitle(e.target.value)}
            onBlur={() => handleTitleUpdate()}
            onKeyDown={(e) => e.key === "Enter" && inputRef.current.blur()}
            style={completed ? {textDecoration: 'line-through', color: 'gray'} : {}}
          />
          <button className="deleteButton" onClick={() => deleteTodo(id)}>🗑️</button>
        </div>
    )
}

export default TodoEntity;
package me.mayderson.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import me.mayderson.todolist.utils.Utils;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping()
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    String userId = request.getAttribute("userId").toString();
    taskModel.setUserId(UUID.fromString(userId));

    var currentDate = LocalDateTime.now();
    if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Start date or end date must be greater than current date");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("End date must be greater than start date");
    }

    var task = this.taskRepository.save(taskModel);

    return ResponseEntity.status(HttpStatus.CREATED).body(task);
  }

  @GetMapping()
  public List<TaskModel> list(HttpServletRequest request) {
    String userId = request.getAttribute("userId").toString();

    var tasks = this.taskRepository.findByUserId(UUID.fromString(userId));

    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@PathVariable UUID id, @RequestBody TaskModel taskModel, HttpServletRequest request) {
    String userId = request.getAttribute("userId").toString();
    taskModel.setId(id);

    var task = this.taskRepository.findById(id);
    if (task.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
    }

    if (!task.get().getUserId().equals(UUID.fromString(userId))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this task");
    }

    Utils.copyNonNullProperties(taskModel, task.get());

    var updatedTask = this.taskRepository.save(task.get());

    return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
  }
}

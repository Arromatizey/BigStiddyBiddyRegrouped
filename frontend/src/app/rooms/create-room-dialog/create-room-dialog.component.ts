import { Component, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Room } from '../../shared/models/room.models';

@Component({
  selector: 'app-create-room-dialog',
  templateUrl: './create-room-dialog.component.html',
  styleUrls: ['./create-room-dialog.component.css']
})
export class CreateRoomDialogComponent {
  @Output() roomCreated = new EventEmitter<Room>();
  @Output() dialogClosed = new EventEmitter<void>();

  createRoomForm: FormGroup;
  isVisible = false;

  constructor(private fb: FormBuilder) {
    this.createRoomForm = this.fb.group({
      subject: ['', Validators.required],
      level: ['', Validators.required],
      topic: [''],
      institution: [''],
      focusDuration: [25, [Validators.required, Validators.min(1)]],
      breakDuration: [5, [Validators.required, Validators.min(1)]]
    });
  }

  show(): void {
    this.isVisible = true;
  }

  hide(): void {
    this.isVisible = false;
    this.dialogClosed.emit();
  }

  onSubmit(): void {
    if (this.createRoomForm.valid) {
      const newRoom: Room = {
        ...this.createRoomForm.value,
        isActive: true,
        timerRunning: false,
        isOnBreak: false
      };
      
      this.roomCreated.emit(newRoom);
      this.hide();
      this.createRoomForm.reset({
        focusDuration: 25,
        breakDuration: 5
      });
    }
  }
}

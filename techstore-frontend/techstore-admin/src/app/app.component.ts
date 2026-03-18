import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthInitializerService } from './core/services/auth-initializer.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'techstore-admin';

    constructor(private authInitializer: AuthInitializerService) {}

  ngOnInit(): void {
    this.authInitializer.init();
  }
}

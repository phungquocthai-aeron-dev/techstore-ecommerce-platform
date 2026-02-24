import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/auth.component';
import { HomeComponent } from './features/home/home.component';

export const routes: Routes = [
    {
        path: '',
        component: LoginComponent
    },
    {
        path: 'home',
        component: HomeComponent
    },
];

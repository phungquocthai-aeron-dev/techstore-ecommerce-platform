import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../shared/models/api-response.model';
import { StaffResponse} from './models/staff.model';


@Injectable({
  providedIn: 'root'
})
export class StaffService {

  private baseUrl = environment.userUrl + '/staffs';

  constructor(private http: HttpClient) {}

  // GET /staffs/{id}
  getById(id: number): Observable<ApiResponse<StaffResponse>> {
    return this.http.get<ApiResponse<StaffResponse>>(
      `${this.baseUrl}/${id}`
    );
  }

  // GET /staffs?id=&email=&phone=
  getOne(params: {
    id?: number;
    email?: string;
    phone?: string;
  }): Observable<ApiResponse<StaffResponse>> {
    let httpParams = new HttpParams();
    if (params.id != null) httpParams = httpParams.set('id', params.id);
    if (params.email)      httpParams = httpParams.set('email', params.email);
    if (params.phone)      httpParams = httpParams.set('phone', params.phone);

    return this.http.get<ApiResponse<StaffResponse>>(
      `${this.baseUrl}`,
      { params: httpParams }
    );
  }

  // GET /staffs/chat-available
  getChatAvailableStaff(): Observable<ApiResponse<StaffResponse[]>> {
    return this.http.get<ApiResponse<StaffResponse[]>>(
      `${this.baseUrl}/chat-available`
    );
  }

  // GET /staffs/by-role?roleName=
  getByRole(roleName: string): Observable<ApiResponse<StaffResponse[]>> {
    const params = new HttpParams().set('roleName', roleName);
  
    return this.http.get<ApiResponse<StaffResponse[]>>(
      `${this.baseUrl}/by-role`,
      { params }
    );
  }

  // GET /staffs/search
  search(params: {
    id?: number;
    email?: string;
    phone?: string;
    fullName?: string;
  }): Observable<ApiResponse<StaffResponse[]>> {
    let httpParams = new HttpParams();
    if (params.id != null)  httpParams = httpParams.set('id', params.id);
    if (params.email)       httpParams = httpParams.set('email', params.email);
    if (params.phone)       httpParams = httpParams.set('phone', params.phone);
    if (params.fullName)    httpParams = httpParams.set('fullName', params.fullName);

    return this.http.get<ApiResponse<StaffResponse[]>>(
      `${this.baseUrl}/search`,
      { params: httpParams }
    );
  }
}
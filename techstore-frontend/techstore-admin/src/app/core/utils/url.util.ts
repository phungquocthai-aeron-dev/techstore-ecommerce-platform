import { environment } from "../../../environments/environment";

export class UrlUtil {
    static imageProduct(path: string): string {
        return `${environment.imageProductUrl}/${path}`;
    }
    static imageAvatar(path: string): string {
        return `${environment.imageAvatarUrl}/${path}`;
    }
}
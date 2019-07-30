declare module 'react-native-smilecat-photo-picker' {

    export interface PhotoPickerResponse {
        result_code: number;
        photo_list: PhotoPickerData[];
    }

    export interface PhotoPickerData {
        imageUri: string;
        width: number;
        height: number;
        thumbnailUri: string;
        thumbnailWidth: number;
        thumbnailHeight: number;
        orientation: number; // ios only
    }

    export default class SmilecatPhotoPicker {
        static open(
            command: 'default' | 'clear',
        ): Promise<PhotoPickerResponse>;
    }
}

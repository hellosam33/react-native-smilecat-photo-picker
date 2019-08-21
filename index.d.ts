declare module 'react-native-smilecat-photo-picker' {

    export interface PhotoPickerResponse {
        result_code: number;
        photo_list: PhotoPickerData[];
    }

    export interface PhotoPickerData {
        imageUri: string;
        imageId: number;
        width: number;
        height: number;
    }

    export default class SmilecatPhotoPicker {
        static open(
            command: 'default' | 'clear',
        ): Promise<PhotoPickerResponse>;
    }
}

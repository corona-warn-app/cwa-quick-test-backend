Longterm-Archive

The Longterm-Archive is a stand-alone database.
QuickTestArchives are moved to the Longterm-Archive using a cronjob (ArchiveService) and encrypted. Depending on the setting, a point in time in the past is set and all entries affected before that are moved. 
During the move, the QuickTestArchive is first converted to a DTO and then to a JSON. The JSON is encrypted and stored in the Longterm-Archive database. 

The object in the long-term archive contains the following fields and information: 
| name        | description
|-------------|-------------
| hashed_guid | Hashed GUID, is taken from QuickTestArchive. Field serves as ID and must be unique.
| identifier  | The identifier is the shortened date of birth (ddMM) and the first 2 letters from the last name. The identifier is used to quickly find an encrypted entry. In the DB the identifier is stored as a hash. The hash is enriched with Pepper.
| key         | The password to decrypt the "ciphertext", encrypted in the DB with RSA. 
| ciphertext  | DTO encrypted with AES as JSON with the content from QuickTestArchive. The generated password is stored in the "key" field.
| public_key  | The RSA PublicKey used to encrypt the password "key", as a Base64 string. 
| algorithm   | The AES algorithm used for encryption, this must also be used for decryption.
| created_at  | Timestamp creation time
| updated_at  | Last modification time
| version     | Field managed by Hibernate

The PublicKey is queried by a hardware module (HSM) and used for encryption by the AES Password. The AES password is generated and stored in the key field after encryption of the content. 
